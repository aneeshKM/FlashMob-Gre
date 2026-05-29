import { BrowserRouter, Route, Routes } from 'react-router';
import FlashSet from '../FlashSet/FlashSet';
import FlashCardsPage from '../FlashCards/FlashCards';

const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<FlashSet />} />
        <Route path="/set" element={<FlashCardsPage />} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
