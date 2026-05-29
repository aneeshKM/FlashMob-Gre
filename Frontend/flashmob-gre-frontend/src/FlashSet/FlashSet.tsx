import { Card, Row, Col, Container } from "react-bootstrap";
import { useNavigate } from "react-router";
import "./FlashSet.css";

const dataset = [
    { id: 1, name: "Set of All words", words: 100 },
    { id: 2, name: "Set 1", words: 30 },
    { id: 3, name: "Set 2", words: 30 },
    { id: 4, name: "Set 3", words: 30 },
    { id: 5, name: "Set 4", words: 30 },
];

const FlashSet = () => {
    const navigate = useNavigate();

    const nextPage = () => {
        navigate("/set");
    };

    return (
        <Container fluid>
            <Row>
                <h1 className="custom-h1">Practice Sets</h1>
            </Row>
            {dataset.map((data) => (
                <Row key={data.id} className="mb-3">
                    <Col>
                        <Card onClick={nextPage}>
                            <Card.Body className="custom-card">
                                <Card.Title>{data.name}</Card.Title>
                                <Card.Text>Total number of practice words are {data.words}</Card.Text>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            ))}
        </Container>
    );
};

export default FlashSet;
