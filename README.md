# NotesToMessenger
Java project for extracting my notes/question-answer memory cards from one of the repositories (e.g. dynamo db), and then sending them into messangers (e.g. telegram channel).
Currently the project is implemented as lambda handler, which extracts question-answer items from dynamo db, and sends them to the specific telegram channel:
- Questions are sent as separate messages to the main, questions channel
- Answers are sent as comments to the question message, so that answers are not visible during reading the questions, and people can discuss answers.

The project is configured to be built and deployed using Github Actions and AWS SAM.

Link to the telegram channel with question-answer cards around software development topics:
- https://t.me/swd_cards
