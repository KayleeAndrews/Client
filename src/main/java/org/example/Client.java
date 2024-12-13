package org.example;

import edu.augusta.sccs.trivia.Question;
import edu.augusta.sccs.trivia.QuestionsReply;
import edu.augusta.sccs.trivia.QuestionsRequest;
import edu.augusta.sccs.trivia.TriviaQuestionsGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import edu.augusta.sccs.trivia.*;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;



//System.err, printLn

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Replace with Docker container address if not local
        int serverPort = 50051;

        // Create a gRPC channel to connect to the server
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();

        // Create a stub for interacting with the server
        TriviaQuestionsGrpc.TriviaQuestionsBlockingStub stub = TriviaQuestionsGrpc.newBlockingStub(channel);

        // Register a player
        String playerId = registerPlayer(stub);

        // Fetch questions
        fetchQuestions(stub, playerId, 10, 2); // Fetch 10 questions at difficulty level 2

        // Shutdown the channel
        channel.shutdown();
    }

    /**
     * Registers a new player with the Trivia Server.
     * @param stub The gRPC stub for interacting with the server.
     * @return The UUID of the registered player.
     */
    private static String registerPlayer(TriviaQuestionsGrpc.TriviaQuestionsBlockingStub stub) {
        // Create a Player object
        Player player = Player.newBuilder()
                .setUuid(UUID.randomUUID().toString()) // Generate a random UUID
                .setUsername("TestPlayer") // Replace with dynamic username if needed
                .setLastDifficulty(1) // Initial difficulty level
                .build();

        // Create a PlayerRegistrationRequest
        PlayerRegistrationRequest request = PlayerRegistrationRequest.newBuilder()
                .setPlayer(player)
                .build();

        // Make the gRPC call to register the player
        PlayerReply response = stub.registerPlayer(request);

        // Log and return the registered player's UUID
        System.out.println("Player registered: " + response.getPlayer().getUuid());
        return response.getPlayer().getUuid();
    }

    /**
     * Fetches a set of questions from the Trivia Server.
     * @param stub The gRPC stub for interacting with the server.
     * @param playerId The UUID of the player requesting questions.
     * @param numberOfQuestions The number of questions to fetch.
     * @param difficulty The difficulty level of the questions.
     */
    private static void fetchQuestions(TriviaQuestionsGrpc.TriviaQuestionsBlockingStub stub, String playerId, int numberOfQuestions, int difficulty) {
        // Create a QuestionsRequest
        QuestionsRequest request = QuestionsRequest.newBuilder()
                .setNumberOfQuestions(numberOfQuestions)
                .setDifficulty(difficulty)
                .build();

        // Make the gRPC call to fetch questions
        QuestionsReply response = stub.getQuestions(request);

        // Process and log the fetched questions
        List<Question> questions = response.getQuestionsList();
        System.out.println("Fetched " + questions.size() + " questions:");
        for (Question question : questions) {
            System.out.println("- Question: " + question.getQuestion());
            System.out.println("  Difficulty: " + question.getDifficulty());
            System.out.println("  Answer Type: " + question.getAnswerType());
            if (question.getChoicesCount() > 0) {
                System.out.println("  Choices: " + question.getChoicesList());
            }
        }
    }
}
