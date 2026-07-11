import { type FormEvent, useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, ButtonGroup, Col, Container, Form, Modal, Row, Spinner } from "react-bootstrap";
import { useNavigate } from "react-router";
import { addWord, addWordsFile, getData, type AddWordPayload } from "../Utils/api";
import { buildPracticeSets } from "../Utils/sets";
import "./FlashSet.css";

type AddWordMode = "single" | "file";

const letterPattern = /\p{Letter}/u;
const devanagariPattern = /\p{Script=Devanagari}/u;

const emptyWordForm: AddWordPayload = {
    word: "",
    marathiMeaning: "",
    englishMeaning: "",
    sampleSentence: "",
};

const isDevanagariMeaning = (value: string) => {
    let hasDevanagariLetter = false;

    for (const character of value) {
        if (!letterPattern.test(character)) {
            continue;
        }

        if (!devanagariPattern.test(character)) {
            return false;
        }

        hasDevanagariLetter = true;
    }

    return hasDevanagariLetter;
};

const FlashSet = () => {
    const navigate = useNavigate();
    const [totalWords, setTotalWords] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [wordForm, setWordForm] = useState<AddWordPayload>({ ...emptyWordForm });
    const [submittingWord, setSubmittingWord] = useState(false);
    const [importingFile, setImportingFile] = useState(false);
    const [showAddWordModal, setShowAddWordModal] = useState(false);
    const [addWordMode, setAddWordMode] = useState<AddWordMode>("single");
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [fileInputKey, setFileInputKey] = useState(0);
    const [wordAdditionPassword, setWordAdditionPassword] = useState("");
    const [addWordStatus, setAddWordStatus] = useState<{
        type: "success" | "danger";
        message: string;
    } | null>(null);

    useEffect(() => {
        let active = true;

        const fetchTotalWords = async () => {
            try {
                const words = await getData();

                if (active) {
                    setTotalWords(words.length);
                }
            } catch {
                if (active) {
                    setError("Unable to load practice sets.");
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        };

        fetchTotalWords();

        return () => {
            active = false;
        };
    }, []);

    const practiceSets = useMemo(() => buildPracticeSets(totalWords), [totalWords]);
    const marathiMeaningInvalid = wordForm.marathiMeaning.trim() !== ""
        && !isDevanagariMeaning(wordForm.marathiMeaning);

    const openAddWordModal = () => {
        setAddWordStatus(null);
        setShowAddWordModal(true);
    };

    const closeAddWordModal = () => {
        if (submittingWord || importingFile) {
            return;
        }

        setShowAddWordModal(false);
    };

    const switchAddWordMode = (mode: AddWordMode) => {
        setAddWordMode(mode);
        setAddWordStatus(null);
    };

    const updateWordForm = (field: keyof AddWordPayload, value: string) => {
        setWordForm((currentForm) => ({
            ...currentForm,
            [field]: value,
        }));
    };

    const submitWord = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        if (!isDevanagariMeaning(wordForm.marathiMeaning)) {
            setAddWordStatus({
                type: "danger",
                message: "Marathi meaning must be written in Devanagari script.",
            });
            return;
        }

        if (!wordAdditionPassword) {
            setAddWordStatus({
                type: "danger",
                message: "Enter the word addition password.",
            });
            return;
        }

        setSubmittingWord(true);
        setAddWordStatus(null);

        try {
            const addedWord = await addWord({
                ...wordForm,
                word: wordForm.word.trim().replace(/\s+/g, " "),
            }, wordAdditionPassword);

            setTotalWords((currentTotal) => Math.max(currentTotal + 1, addedWord.id));
            setWordForm({ ...emptyWordForm });
            setAddWordStatus({
                type: "success",
                message: `Accepted "${addedWord.word}".`,
            });
        } catch (submitError) {
            setAddWordStatus({
                type: "danger",
                message: submitError instanceof Error ? submitError.message : "Unable to add word.",
            });
        } finally {
            setSubmittingWord(false);
        }
    };

    const submitFile = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setImportingFile(true);
        setAddWordStatus(null);

        try {
            if (!wordAdditionPassword) {
                throw new Error("Enter the word addition password.");
            }

            if (!selectedFile) {
                throw new Error("Choose an Excel or CSV file first.");
            }

            const result = await addWordsFile(selectedFile, wordAdditionPassword);

            setTotalWords((currentTotal) => currentTotal + result.addedCount);
            setSelectedFile(null);
            setFileInputKey((currentKey) => currentKey + 1);
            setAddWordStatus({
                type: result.addedCount > 0 ? "success" : "danger",
                message: `Added ${result.addedCount} unique words. Skipped ${result.skippedCount}.`,
            });
        } catch (submitError) {
            setAddWordStatus({
                type: "danger",
                message: submitError instanceof Error ? submitError.message : "Unable to import words.",
            });
        } finally {
            setImportingFile(false);
        }
    };

    return (
        <main className="flash-set-page">
            <Container className="py-4 py-md-5">
                <Row className="align-items-end g-3 mb-4">
                    <Col lg={8}>
                        <p className="page-kicker">GRE Vocabulary</p>
                        <h1 className="page-title">Practice Sets</h1>
                        <p className="page-subtitle">
                            Study every word together or choose a 50-word batch.
                        </p>
                    </Col>
                    <Col lg={4} className="header-actions">
                        <Badge bg="light" text="dark" className="word-total-badge">
                            {totalWords} words
                        </Badge>
                        <Button className="add-word-open-button" onClick={openAddWordModal}>
                            Add New Word
                        </Button>
                    </Col>
                </Row>

                <Modal className="add-word-modal" show={showAddWordModal} onHide={closeAddWordModal} centered size="lg">
                    <Modal.Header closeButton>
                        <Modal.Title>Add New Word</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <ButtonGroup className="add-word-mode-toggle" aria-label="Add word mode">
                            <Button
                                type="button"
                                variant={addWordMode === "single" ? "primary" : "outline-secondary"}
                                onClick={() => switchAddWordMode("single")}
                            >
                                Single Word
                            </Button>
                            <Button
                                type="button"
                                variant={addWordMode === "file" ? "primary" : "outline-secondary"}
                                onClick={() => switchAddWordMode("file")}
                            >
                                Excel / CSV
                            </Button>
                        </ButtonGroup>

                        {addWordStatus && (
                            <Alert variant={addWordStatus.type} className="mb-3">
                                {addWordStatus.message}
                            </Alert>
                        )}

                        <Form.Group controlId="wordAdditionPassword" className="mb-3">
                            <Form.Label>Word Addition Password</Form.Label>
                            <Form.Control
                                type="password"
                                value={wordAdditionPassword}
                                onChange={(event) => setWordAdditionPassword(event.target.value)}
                                placeholder="Required to add or import words"
                                autoComplete="current-password"
                                required
                            />
                            <Form.Text>
                                This password is checked by the backend and is not stored in the app.
                            </Form.Text>
                        </Form.Group>

                        {addWordMode === "single" && (
                            <Form onSubmit={submitWord}>
                                <Row className="g-3">
                                    <Col md={6}>
                                        <Form.Group controlId="word">
                                            <Form.Label>English Word</Form.Label>
                                            <Form.Control
                                                value={wordForm.word}
                                                onChange={(event) => updateWordForm("word", event.target.value)}
                                                placeholder="e.g. Resolute"
                                                required
                                            />
                                        </Form.Group>
                                    </Col>

                                    <Col md={6}>
                                        <Form.Group controlId="marathiMeaning">
                                            <Form.Label>Marathi Meaning</Form.Label>
                                            <Form.Control
                                                value={wordForm.marathiMeaning}
                                                onChange={(event) => updateWordForm("marathiMeaning", event.target.value)}
                                                placeholder="उदा. ठाम"
                                                isInvalid={marathiMeaningInvalid}
                                                required
                                            />
                                            <Form.Control.Feedback type="invalid">
                                                Marathi meaning must be written in Devanagari script.
                                            </Form.Control.Feedback>
                                        </Form.Group>
                                    </Col>

                                    <Col md={6}>
                                        <Form.Group controlId="englishMeaning">
                                            <Form.Label>English Meaning</Form.Label>
                                            <Form.Control
                                                value={wordForm.englishMeaning}
                                                onChange={(event) => updateWordForm("englishMeaning", event.target.value)}
                                                placeholder="Optional"
                                            />
                                        </Form.Group>
                                    </Col>

                                    <Col md={6}>
                                        <Form.Group controlId="sampleSentence">
                                            <Form.Label>Sample Sentence</Form.Label>
                                            <Form.Control
                                                value={wordForm.sampleSentence}
                                                onChange={(event) => updateWordForm("sampleSentence", event.target.value)}
                                                placeholder="Use it in a sentence"
                                                required
                                            />
                                        </Form.Group>
                                    </Col>
                                </Row>

                                <div className="add-word-actions">
                                    <Button type="submit" disabled={submittingWord}>
                                        {submittingWord ? "Adding..." : "Add Word"}
                                    </Button>
                                </div>
                            </Form>
                        )}

                        {addWordMode === "file" && (
                            <Form onSubmit={submitFile}>
                                <Form.Group controlId="wordFile">
                                    <Form.Label>Excel or CSV File</Form.Label>
                                    <Form.Control
                                        key={fileInputKey}
                                        type="file"
                                        accept=".xlsx,.xls,.csv"
                                        onChange={(event) => {
                                            const input = event.currentTarget as HTMLInputElement;
                                            setSelectedFile(input.files?.[0] ?? null);
                                        }}
                                        required
                                    />
                                    <Form.Text>
                                        Use columns: Word, Marathi Meaning, English Meaning, Sample Sentence.
                                    </Form.Text>
                                </Form.Group>

                                <div className="add-word-actions">
                                    <Button type="submit" disabled={importingFile}>
                                        {importingFile ? "Importing..." : "Import Unique Words"}
                                    </Button>
                                </div>
                            </Form>
                        )}
                    </Modal.Body>
                </Modal>

                {loading && (
                    <div className="set-state-panel">
                        <Spinner animation="border" role="status" />
                        <span>Loading words...</span>
                    </div>
                )}

                {error && <Alert variant="danger">{error}</Alert>}

                {!loading && !error && (
                    <Row className="g-3">
                        {practiceSets.map((practiceSet) => (
                            <Col key={practiceSet.id} md={6} xl={4}>
                                <button
                                    type="button"
                                    className="practice-set-card"
                                    onClick={() => navigate(practiceSet.path)}
                                >
                                    <span className="set-card-copy">
                                        <span className="set-card-title">{practiceSet.name}</span>
                                        <span className="set-card-range">{practiceSet.rangeLabel}</span>
                                    </span>
                                    <span className="set-card-count">
                                        <span>{practiceSet.wordCount}</span>
                                        <span>words</span>
                                    </span>
                                </button>
                            </Col>
                        ))}
                    </Row>
                )}
            </Container>
        </main>
    );
};

export default FlashSet;
