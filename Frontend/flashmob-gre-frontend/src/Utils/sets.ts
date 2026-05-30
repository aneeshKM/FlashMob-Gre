export const SET_SIZE = 50;

export interface PracticeSet {
    id: string;
    name: string;
    rangeLabel: string;
    wordCount: number;
    path: string;
}

export interface SelectedSet {
    id: string;
    name: string;
    offset?: number;
    limit?: number;
}

export const buildPracticeSets = (totalWords: number): PracticeSet[] => {
    const safeTotal = Math.max(totalWords, 0);
    const sets: PracticeSet[] = [
        {
            id: 'all',
            name: 'All Words',
            rangeLabel: 'Complete list',
            wordCount: safeTotal,
            path: '/set/all',
        },
    ];

    const chunkCount = Math.ceil(safeTotal / SET_SIZE);

    for (let index = 0; index < chunkCount; index += 1) {
        const start = index * SET_SIZE + 1;
        const end = Math.min((index + 1) * SET_SIZE, safeTotal);

        sets.push({
            id: String(index + 1),
            name: `Set ${index + 1}`,
            rangeLabel: `Words ${start}-${end}`,
            wordCount: end - start + 1,
            path: `/set/${index + 1}`,
        });
    }

    return sets;
};

export const getSelectedSet = (setId?: string): SelectedSet => {
    const normalizedSetId = setId?.toLowerCase();

    if (!normalizedSetId || normalizedSetId === 'all') {
        return {
            id: 'all',
            name: 'All Words',
        };
    }

    const setNumber = Number(normalizedSetId);

    if (!Number.isInteger(setNumber) || setNumber < 1) {
        return {
            id: 'all',
            name: 'All Words',
        };
    }

    return {
        id: String(setNumber),
        name: `Set ${setNumber}`,
        offset: (setNumber - 1) * SET_SIZE,
        limit: SET_SIZE,
    };
};
