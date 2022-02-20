# NotesToMessenger
Java project for extracting my notes/question-answer memory cards from one of the repositories (e.g. dynamo db), and then sending them into messangers (e.g. telegram channel).
Currently the project is implemented as lambda handler, which extracts question-answer items from dynamo db, and sends them to the specific telegram channel:
- Questions are sent as separate messages to the main, questions channel
- Answers are sent as comments to the question message, so that answers are not visible during reading the questions, and people can discuss answers.

The project is configured to be built and deployed using Github Actions and AWS SAM using following pipeline:
- For pushes into all branches:
  - Unit tests are executed
- For pushes into master and develop branches, as well for tags.
  - Dev environment is created
  - Integration tests are executed against dev environment
- For tags pushes
  - Prod environment is created

Prerequisites:
- DynamoDB tables for storing notes and notes status needs to be created in advance, and relevant environment variables are set 
- Telegram tokens and chats are created in advance, and relevant environment variables are set
- AWS Credentials are created in advance, and relevant environment variables/configuration files are set

Link to the telegram channel with question-answer cards around software development topics:
- https://t.me/swd_cards
