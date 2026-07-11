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

export interface AddWordPayload {
    word: string;
    marathiMeaning: string;
    englishMeaning?: string;
    sampleSentence: string;
}

export interface SkippedWord {
    rowNumber: number;
    word: string;
    reason: string;
}

export interface WordImportResult {
    totalRows: number;
    addedWords: Word[];
    skippedWords: SkippedWord[];
    addedCount: number;
    skippedCount: number;
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

const WORD_ADDITION_PASSWORD_HEADER = "X-Word-Addition-Password";

export const addWord = async (payload: AddWordPayload, password: string): Promise<Word> => {
    const response = await fetch(`${API_BASE_URL}/addWord`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            [WORD_ADDITION_PASSWORD_HEADER]: password,
        },
        body: JSON.stringify(payload),
    });

    if (!response.ok) {
        let message = `Failed to add word: ${response.status}`;

        try {
            const body = await response.json() as { message?: string };
            message = body.message ?? message;
        } catch {
            // Keep the HTTP status message if the response body is not JSON.
        }

        throw new Error(message);
    }

    return response.json() as Promise<Word>;
};

export const addWordsFile = async (file: File, password: string): Promise<WordImportResult> => {
    const formData = new FormData();
    formData.set("file", file);

    const response = await fetch(`${API_BASE_URL}/addWordsFile`, {
        method: "POST",
        headers: {
            [WORD_ADDITION_PASSWORD_HEADER]: password,
        },
        body: formData,
    });

    if (!response.ok) {
        let message = `Failed to import words: ${response.status}`;

        try {
            const body = await response.json() as { message?: string };
            message = body.message ?? message;
        } catch {
            // Keep the HTTP status message if the response body is not JSON.
        }

        throw new Error(message);
    }

    return response.json() as Promise<WordImportResult>;
};
