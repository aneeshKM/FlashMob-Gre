import { useEffect, useState } from 'react';
import { Carousel, Row, Col, Card, Container } from 'react-bootstrap';
import { getData, type Word } from '../Utils/api';
import './FlashCards.css';

const FlashCardsPage = () => {
    const [data, setData] = useState<Word[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let active = true;

        const fetchDataFromApi = async () => {
            try {
                const fetchedData = await getData();
                if (active) {
                    setData(fetchedData);
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
    }, []);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <Container>
            <h1>FlashCards</h1>
            <Row>
                <Col>
                    <Carousel interval={null}>
                        {data.map((flashcard) => (
                            <Carousel.Item key={flashcard.id}>
                                <Card className='flashcard'>
                                    <Card.Body>
                                        <Card.Title><h2>Word:</h2></Card.Title>
                                        <Card.Text as="div">
                                            <h4>{flashcard.id}) {flashcard.word}</h4>
                                        </Card.Text>
                                        <Card.Title><h2>Meaning and Example:</h2></Card.Title>
                                        <Card.Text as="div">
                                            <h4>Marathi Meaning: {flashcard.marathiMeaning}</h4>
                                            <h4>English Meaning: {flashcard.englishMeaning}</h4>
                                            <h4>Sample Sentence: {flashcard.sampleSentence}</h4>
                                        </Card.Text>
                                    </Card.Body>
                                </Card>
                            </Carousel.Item>
                        ))}
                    </Carousel>
                </Col>
            </Row>
            <Row>
                {data.map((flashcards) => (
                    <Col key={flashcards.id} sm="2">
                        <Card className='wordCard'>
                            <Card.Title>{flashcards.id}: {flashcards.word}</Card.Title>
                        </Card>
                    </Col>
                ))}
            </Row>
        </Container>
    );
};


export default FlashCardsPage;
