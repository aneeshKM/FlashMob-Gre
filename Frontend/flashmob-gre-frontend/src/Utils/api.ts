export interface Word {
    id: number;
    word: string;
    marathiMeaning: string;
    englishMeaning: string;
    sampleSentence: string;
}

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "/api").replace(/\/$/, "");

export const getData = async (): Promise<Word[]> => {
    const response = await fetch(`${API_BASE_URL}/getWordData`);

    if (!response.ok) {
        throw new Error(`Failed to fetch word data: ${response.status}`);
    }

    return response.json() as Promise<Word[]>;
};
