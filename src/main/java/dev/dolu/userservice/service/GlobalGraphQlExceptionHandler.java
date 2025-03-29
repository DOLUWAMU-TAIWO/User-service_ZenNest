package dev.dolu.userservice.service;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GlobalGraphQlExceptionHandler extends DataFetcherExceptionResolverAdapter {

    private static final Logger logger = LoggerFactory.getLogger(GlobalGraphQlExceptionHandler.class);

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        // Log error details along with the field where the error occurred
        logger.error("GraphQL error in field '{}': {}", env.getField().getName(), ex.getMessage(), ex);

        // 1. Handle type conversion errors (e.g., invalid UUID format)
        if (ex instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException matme = (MethodArgumentTypeMismatchException) ex;
            String errorMessage = String.format("Invalid value '%s' for argument '%s'. Expected type: %s.",
                    matme.getValue(), matme.getName(), matme.getRequiredType().getSimpleName());
            return GraphqlErrorBuilder.newError(env)
                    .message(errorMessage)
                    .errorType(ErrorType.BAD_REQUEST)
                    .build();
        }
        // 2. Handle conversion failures (like invalid UUID strings)
        else if (ex.getMessage() != null && ex.getMessage().contains("Failed to convert argument value")) {
            String errorMessage = "Invalid argument provided. " + ex.getMessage();
            return GraphqlErrorBuilder.newError(env)
                    .message(errorMessage)
                    .errorType(ErrorType.BAD_REQUEST)
                    .build();
        }
        // 3. Handle errors thrown as ResponseStatusException (from our service layer)
        else if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            return GraphqlErrorBuilder.newError(env)
                    .message(rse.getReason())
                    .errorType(ErrorType.BAD_REQUEST)
                    .build();
        }
        // 4. Additional dynamic handling can be added here for other exception types

        // Fallback for any other exceptions – treated as internal server errors
        return GraphqlErrorBuilder.newError(env)
                .message("Internal Server Error: " + ex.getMessage())
                .errorType(ErrorType.INTERNAL_ERROR)
                .build();
    }
}