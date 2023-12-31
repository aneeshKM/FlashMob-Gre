import React from 'react';
import { BrowserRouter, Route, Routes, } from 'react-router-dom';
import FlashSet from '../FlashSet/FlashSet';
import FlashCardsPage from '../FlashCards/FlashCards';

const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
          <Route path="/" element={<FlashSet/>} />
          <Route path="/set" element={<FlashCardsPage/>}/>
      </Routes>
    </BrowserRouter>
  );
};

export default App;