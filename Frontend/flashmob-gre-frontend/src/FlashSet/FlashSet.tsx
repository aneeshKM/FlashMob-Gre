import { useEffect, useMemo, useState } from "react";
import { Alert, Badge, Col, Container, Row, Spinner } from "react-bootstrap";
import { useNavigate } from "react-router";
import { getData } from "../Utils/api";
import { buildPracticeSets } from "../Utils/sets";
import "./FlashSet.css";

const FlashSet = () => {
    const navigate = useNavigate();
    const [totalWords, setTotalWords] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

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
                    <Col lg={4} className="text-lg-end">
                        <Badge bg="light" text="dark" className="word-total-badge">
                            {totalWords} words
                        </Badge>
                    </Col>
                </Row>

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
