export interface Word {
    id: number;
    word: string;
    marathiMeaning: string;
    englishMeaning: string;
    sampleSentence: string;
}

export interface GetDataOptions {
    offset?: number;
    limit?: number;
}

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "/api").replace(/\/$/, "");

export const getData = async (options: GetDataOptions = {}): Promise<Word[]> => {
    const params = new URLSearchParams();

    if (options.offset !== undefined) {
        params.set("offset", String(options.offset));
    }

    if (options.limit !== undefined) {
        params.set("limit", String(options.limit));
    }

    const query = params.toString();
    const response = await fetch(`${API_BASE_URL}/getWordData${query ? `?${query}` : ""}`);

    if (!response.ok) {
        throw new Error(`Failed to fetch word data: ${response.status}`);
    }

    return response.json() as Promise<Word[]>;
};
