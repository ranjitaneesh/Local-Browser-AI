let currentSessionId = null;
let thinkingMessageId = null;
const stompClient = Stomp.over(new SockJS('/ollama-websocket'));

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);

    stompClient.subscribe('/topic/sessions', (response) => {
        const sessionData = JSON.parse(response.body);
        currentSessionId = sessionData.sessionId;
        addSessionToSidebar(sessionData);
    });

    stompClient.subscribe('/topic/responses', (response) => {
        const data = JSON.parse(response.body);
        if (data.sessionId === currentSessionId) {
            if (thinkingMessageId) {
                removeThinkingIndicator();
            }
            addMessage('bot', data.content);
        }
    });

    stompClient.subscribe('/topic/errors', (error) => {
        const errorData = JSON.parse(error.body);
        showError(errorData.content);
    });
});

function createNewSession() {
    const model = document.getElementById('model-select').value;
    stompClient.send("/app/create-session", {},
        JSON.stringify({ model: model })
    );
}

function addSessionToSidebar(sessionData) {
    const sessionList = document.querySelector('#session-list');
    const sessionElement = document.createElement('div');
    sessionElement.className = 'session-item';
    sessionElement.innerHTML = `
        <div class="session-info">
            <div class="session-model">${sessionData.model}</div>
            <div class="session-details">
                <span>${new Date().toLocaleTimeString()}</span>
                <span>${sessionData.sessionId.substring(0, 6)}</span>
            </div>
        </div>
    `;
    sessionElement.onclick = () => switchSession(sessionData.sessionId);
    sessionList.prepend(sessionElement);
    switchSession(sessionData.sessionId);
}

async function switchSession(sessionId) {
    currentSessionId = sessionId;
    const chatWindow = document.getElementById('chat-window');
    chatWindow.innerHTML = '';

    try {
        const response = await fetch(`/session-history?sessionId=${sessionId}`);
        const history = await response.json();
        history.forEach(msg => addMessage(msg.role.toLowerCase(), msg.content));
    } catch (error) {
        showError('Failed to load session history');
    }
}

function submitPrompt() {
    if (!currentSessionId) {
        showError('Please create or select a session first');
        return;
    }

    const promptInput = document.getElementById('prompt-input');
    const prompt = promptInput.value.trim();
    if (!prompt) return;

    addMessage('user', prompt);
    showThinkingIndicator();

    stompClient.send("/app/submit-prompt", {},
        JSON.stringify({
            sessionId: currentSessionId,
            prompt: prompt
        })
    );
    promptInput.value = '';
}

function showThinkingIndicator() {
    const chatWindow = document.getElementById('chat-window');
    const thinkingDiv = document.createElement('div');
    thinkingDiv.className = 'message thinking-indicator';
    thinkingDiv.innerHTML = `
        <div class="dot-flashing"></div>
        <div class="dot-flashing" style="animation-delay: 0.2s"></div>
        <div class="dot-flashing" style="animation-delay: 0.4s"></div>
    `;
    chatWindow.appendChild(thinkingDiv);
    thinkingMessageId = thinkingDiv.id = `thinking-${Date.now()}`;
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

function removeThinkingIndicator() {
    const element = document.getElementById(thinkingMessageId);
    if (element) element.remove();
    thinkingMessageId = null;
}

function addMessage(role, content) {
    const chatWindow = document.getElementById('chat-window');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message';

    // Format code blocks
    const formattedContent = content.replace(/```([\s\S]*?)```/g, '<div class="code-block">$1</div>');

    messageDiv.innerHTML = `
        <div class="${role}-message">${formattedContent}</div>
    `;
    chatWindow.appendChild(messageDiv);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

function handleEnterKey(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        submitPrompt();
    }
}

function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'message error-message';
    errorDiv.textContent = message;
    document.getElementById('chat-window').appendChild(errorDiv);
    setTimeout(() => errorDiv.remove(), 3000);
}