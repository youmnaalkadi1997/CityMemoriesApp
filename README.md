# City Memories

City Memories ist eine Webanwendung, mit der man Erinnerungen an Städte weltweit posten und teilen kann.  
Nutzer können Städte durchsuchen, Kommentare hinterlassen, Likes vergeben und Favoritenlisten erstellen.  
Die App nutzt Echtzeit-Benachrichtigungen und externe APIs für Stadtinformationen.

## Features
- Städte weltweit suchen und Informationen abrufen (mit externen APIs)
- Stadtbeschreibungen mit Bildern und Karten
- Kommentare hinzufügen, bearbeiten und löschen
- Kommentare mit Bildern versehen
- Likes und Antworten auf Kommentare
- Echtzeit-Benachrichtigungen bei Likes/Antworten (WebSocket)
- Beliebte Städte nach Likes sortiert
- Favoritenliste mit Gruppen für die Organisation
- Login/Logout via GitHub OAuth2
- CI/CD Pipeline für automatisches Deployment

## Technologien
- Backend: Java, Spring Boot
- Frontend: React
- Authentifizierung: OAuth2 mit GitHub
- Datenbank: MongoDB
- Echtzeit: WebSockets
- Deployment: Render
- CI/CD: GitHub Actions
- Codequalität: SonarCloud

## Installation & Deployment

```bash
# Repository klonen
git clone https://github.com/youmnaalkadi1997/CityMemoriesApp.git

# Backend starten
cd backend
./mvnw spring-boot:run

# Frontend starten
cd ../frontend
npm install
npm start
```

- Die App ist dann unter: http://localhost:3000 erreichbar.
- Hinweis: Das Backend läuft standardmäßig auf http://localhost:8080 und das Frontend auf http://localhost:3000.
- Deployment live verfügbar unter: https://citymemories.onrender.com
