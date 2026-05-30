import { useEffect, useMemo, useState } from 'react';
import { Alert, Badge, Button, Col, Container, ProgressBar, Row, Spinner } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router';
import { getData, type Word } from '../Utils/api';
import { getSelectedSet } from '../Utils/sets';
import './FlashCards.css';

const FlashCardsPage = () => {
    const navigate = useNavigate();
    const { setId } = useParams();
    const selectedSet = useMemo(() => getSelectedSet(setId), [setId]);
    const [data, setData] = useState<Word[]>([]);
    const [currentIndex, setCurrentIndex] = useState(0);
    const [isFlipped, setIsFlipped] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [hasShownTimedHint, setHasShownTimedHint] = useState(false);
    const [showTimedHint, setShowTimedHint] = useState(false);

    useEffect(() => {
        let active = true;

        const fetchDataFromApi = async () => {
            setLoading(true);
            setError("");

            try {
                const fetchedData = await getData({
                    offset: selectedSet.offset,
                    limit: selectedSet.limit,
                });

                if (active) {
                    setData(fetchedData);
                    setCurrentIndex(0);
                    setIsFlipped(false);
                }
            } catch {
                if (active) {
                    setError("Error fetching data");
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        };

        fetchDataFromApi();

        return () => {
            active = false;
        };
    }, [selectedSet.offset, selectedSet.limit]);

    const activeWord = data[currentIndex];

    useEffect(() => {
        if (!activeWord || isFlipped || hasShownTimedHint) {
            setShowTimedHint(false);
            return undefined;
        }

        const hintDelay = window.setTimeout(() => {
            setShowTimedHint(true);
            setHasShownTimedHint(true);
        }, 5000);

        return () => window.clearTimeout(hintDelay);
    }, [activeWord, isFlipped, hasShownTimedHint]);

    useEffect(() => {
        if (!showTimedHint) {
            return undefined;
        }

        const hideDelay = window.setTimeout(() => {
            setShowTimedHint(false);
        }, 3500);

        return () => window.clearTimeout(hideDelay);
    }, [showTimedHint]);

    const progress = data.length > 0 ? ((currentIndex + 1) / data.length) * 100 : 0;
    const rangeLabel = useMemo(() => {
        if (data.length === 0) {
            return selectedSet.name;
        }

        if (selectedSet.id === 'all') {
            return `${data.length} words`;
        }

        return `Words ${data[0].id}-${data[data.length - 1].id}`;
    }, [data, selectedSet.id, selectedSet.name]);

    const showCard = (nextIndex: number) => {
        setCurrentIndex(nextIndex);
        setIsFlipped(false);
    };

    const showPreviousCard = () => {
        showCard(Math.max(currentIndex - 1, 0));
    };

    const showNextCard = () => {
        showCard(Math.min(currentIndex + 1, data.length - 1));
    };

    const renderContent = () => {
        if (loading) {
            return (
                <div className="flashcards-state-panel">
                    <Spinner animation="border" role="status" />
                    <span>Loading words...</span>
                </div>
            );
        }

        if (error) {
            return <Alert variant="danger">{error}</Alert>;
        }

        if (!activeWord) {
            return <Alert variant="warning">No words found for this set.</Alert>;
        }

        return (
            <>
                <section className="study-panel" aria-label="Flashcard study area">
                    <div className="study-meta">
                        <Badge bg="light" text="dark" className="card-count-badge">
                            Card {currentIndex + 1} of {data.length}
                        </Badge>
                        <ProgressBar now={progress} className="study-progress" aria-label="Study progress" />
                    </div>

                    <div className="study-stage">
                        <Button
                            variant="outline-secondary"
                            className="study-nav-button"
                            onClick={showPreviousCard}
                            disabled={currentIndex === 0}
                        >
                            Previous
                        </Button>

                        <button
                            type="button"
                            className={`flip-card-button${isFlipped ? ' is-flipped' : ''}`}
                            onClick={() => setIsFlipped((flipped) => !flipped)}
                            aria-label={isFlipped ? 'Show word' : 'Show meanings'}
                            aria-pressed={isFlipped}
                        >
                            <span className="flip-card-inner">
                                <span className="flip-card-face flashcard-front">
                                    <span className="flashcard-id">#{activeWord.id}</span>
                                    <span className="flashcard-front-content">
                                        <span className="flashcard-word">{activeWord.word}</span>
                                        <span className={`flashcard-hint${showTimedHint ? ' is-visible' : ''}`}>
                                            Tap card for meaning
                                        </span>
                                    </span>
                                </span>

                                <span className="flip-card-face flashcard-back">
                                    <span className="flashcard-id">#{activeWord.id}</span>
                                    <span className="meaning-row">
                                        <span className="meaning-label">English</span>
                                        <span className="meaning-text">{activeWord.englishMeaning || 'Not available'}</span>
                                    </span>
                                    <span className="meaning-row">
                                        <span className="meaning-label">Marathi</span>
                                        <span className="meaning-text">{activeWord.marathiMeaning || 'Not available'}</span>
                                    </span>
                                    <span className="meaning-row sample-row">
                                        <span className="meaning-label">Sample Sentence</span>
                                        <span className="meaning-text">{activeWord.sampleSentence || 'Not available'}</span>
                                    </span>
                                </span>
                            </span>
                        </button>

                        <Button
                            variant="outline-secondary"
                            className="study-nav-button"
                            onClick={showNextCard}
                            disabled={currentIndex === data.length - 1}
                        >
                            Next
                        </Button>
                    </div>
                </section>

                <section className="word-list-panel" aria-label="Words in this set">
                    <div className="word-list-header">
                        <h2>Words in {selectedSet.name}</h2>
                        <Badge bg="light" text="dark" className="word-list-badge">
                            {data.length}
                        </Badge>
                    </div>

                    <div className="word-chip-grid">
                        {data.map((word, index) => (
                            <button
                                type="button"
                                key={word.id}
                                className={`word-chip${index === currentIndex ? ' is-active' : ''}`}
                                onClick={() => showCard(index)}
                                title={word.word}
                            >
                                <span>{word.id}</span>
                                {word.word}
                            </button>
                        ))}
                    </div>
                </section>
            </>
        );
    };

    return (
        <main className="flashcards-page">
            <Container className="py-4 py-md-5">
                <Row className="align-items-end g-3 mb-4">
                    <Col lg={8}>
                        <Button variant="link" className="back-link" onClick={() => navigate('/')}>
                            Back to sets
                        </Button>
                        <p className="page-kicker">Flashcards</p>
                        <h1 className="page-title">{selectedSet.name}</h1>
                        <p className="page-subtitle">{rangeLabel}</p>
                    </Col>
                    <Col lg={4} className="text-lg-end">
                        <Badge bg="light" text="dark" className="word-total-badge">
                            {data.length} words
                        </Badge>
                    </Col>
                </Row>

                {renderContent()}
            </Container>
        </main>
    );
};

export default FlashCardsPage;
