import ButtonBase from '../../../src/compenents/buttons/ButtonBase';

beforeEach(() => {
    global.fetch = jest.fn();
});
    
afterEach(() => {
    jest.clearAllMocks();
});

describe('ButtonBase PostRequest', () => {
    const TestButton = new ButtonBase('TestButton', '/test-endpoint'); 
    it('should correctly handle text response', async () => {
        const mockTextResponse = 'This is a test text'; 
        global.fetch.mockResolvedValueOnce({
            ok: true, 
            headers: {
                get: () => 'text/plain', 
            },
            text: () => Promise.resolve(mockTextResponse), 
        });

        const response = await TestButton.postRequest({ key: 'data' }); 

        expect(response).toBe(mockTextResponse); 
        expect(global.fetch).toHaveBeenCalledWith(
            `http://localhost:8080/test-endpoint`, 
            {
                method: 'POST', 
                headers: {
                    'Content-Type': 'application/json', 
                },
                body: JSON.stringify({ key: 'data' }), 
            }
        );
    });

    it('should correctly handle JSON response', async () => {
        const mockJsonResponse = { message: 'Success' }; 

        global.fetch.mockResolvedValueOnce({
            ok: true, 
            headers: {
                get: () => 'application/json', 
            },
            json: () => Promise.resolve(mockJsonResponse), 
        });

        const response = await TestButton.postRequest({ key: 'data' }); 

        expect(response).toEqual(mockJsonResponse); 
        expect(global.fetch).toHaveBeenCalledWith(
            `http://localhost:8080/test-endpoint`, 
            {
                method: 'POST', 
                headers: {
                    'Content-Type': 'application/json', 
                },
                body: JSON.stringify({ key: 'data' }), 
            }
        );
    });
});
        