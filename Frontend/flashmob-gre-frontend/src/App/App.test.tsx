import { render, screen } from '@testing-library/react';
import { afterEach, beforeEach, vi } from 'vitest';
import App from './App';

beforeEach(() => {
  vi.stubGlobal(
    'fetch',
    vi.fn().mockResolvedValue({
      ok: true,
      json: async () => [],
    })
  );
});

afterEach(() => {
  vi.unstubAllGlobals();
});

test('renders practice sets page', () => {
  render(<App />);
  expect(screen.getByRole('heading', { name: /practice sets/i })).toBeInTheDocument();
});
