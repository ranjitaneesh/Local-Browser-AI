# Ollama Chat UI - Spring Boot Application

This project is a Spring Boot-based web application that provides a user interface for interacting with the Ollama API. It supports real-time chat sessions, model selection, and session history management. The application uses WebSocket for real-time communication and integrates with the Ollama API for generating responses.

---

## Table of Contents

1. [Features](#features)
2. [Technologies Used](#technologies-used)
3. [Prerequisites](#prerequisites)
4. [Setup and Installation](#setup-and-installation)
5. [Running the Application](#running-the-application)
6. [Project Structure](#project-structure)
7. [API Endpoints](#api-endpoints)
8. [Contributing](#contributing)
9. [License](#license)

---

## Features

- **Real-time Chat Interface**: Interact with the Ollama API in real-time using WebSocket.
- **Session Management**: Create and manage multiple chat sessions with different models.
- **Model Selection**: Choose from different models (e.g., DeepSeek-R1, Mistral, Phi-2).
- **Session History**: View and reload chat history for each session.
- **LaTeX and Code Formatting**: Supports formatted responses with LaTeX equations and code blocks.

---

## Technologies Used

- **Backend**: Spring Boot, WebSocket, Ollama4J (Ollama API client)
- **Frontend**: HTML, CSS, JavaScript (SockJS, STOMP)
- **Build Tool**: Maven
- **Database**: In-memory session storage (no external database required)
- **Other Tools**: OkHttp (for HTTP client), Lombok (for boilerplate code reduction)

---

## Prerequisites

Before running the project, ensure you have the following installed:

1. **Java Development Kit (JDK) 17 or higher**
2. **Maven 3.8.x or higher**
3. **Ollama Server**: Ensure the Ollama server is running locally or accessible at `http://localhost:11434`.
4. **Web Browser**: Modern browser with JavaScript support (e.g., Chrome, Firefox, Edge).

---

## Setup and Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/ollama-chat-ui.git
   cd ollama-chat-ui
   ```
2. **Install Dependencies**:
Use Maven to install the required dependencies:
```bash
mvn clean install
```
3. **Configure Ollama Server**:
Ensure the Ollama server is running and accessible. By default, the application connects to http://localhost:11434. If your Ollama server is running on a different host or port, update the OllamaAPI configuration in OllamaController.java.

4. **Pull Required Models**:
Ensure the required models (e.g., deepseek-r1:7b, mistral, phi) are pulled and available in your Ollama server:
```base
ollama pull deepseek-r1:7b
ollama pull mistral
ollama pull phi
```
## Running the Application
Start the Spring Boot Application:
Run the application using Maven:
```bash
mvn spring-boot:run
```
## Access the Application:
Open your web browser and navigate to:


http://localhost:8080
Use the Chat Interface:

Select a model from the dropdown.

Start a new chat session.

Enter prompts and view responses in real-time.

## Project Structure

ollama-chat-ui/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/ollamaui/
│   │   │       ├── OllamaApplication.java          # Main Spring Boot application
│   │   │       ├── OllamaController.java           # REST and WebSocket controller
│   │   │       ├── WebSocketConfig.java            # WebSocket configuration
│   │   └── resources/
│   │       ├── public/                             # Frontend assets (HTML, CSS, JS)
│   │       └── application.properties              # Configuration file
│   └── test/                                      # Unit tests
├── pom.xml                                        # Maven build configuration
└── README.md                                      # Project documentation

## API Endpoints
WebSocket Endpoints
Create Session: /app/create-session

Submit Prompt: /app/submit-prompt

Session Responses: /topic/responses

Session Errors: /topic/errors

REST Endpoints
Get Session History: /session-history?sessionId={sessionId}

## Contributing
Contributions are welcome! If you'd like to contribute to this project, please follow these steps:

Fork the repository.

Create a new branch for your feature or bugfix.

Commit your changes and push to your branch.

Submit a pull request with a detailed description of your changes.

## License
This project is licensed under the MIT License. See the LICENSE file for details.

Acknowledgments
Ollama API: For providing the backend model integration.

Spring Boot: For the robust backend framework.

SockJS and STOMP: For enabling real-time communication.
