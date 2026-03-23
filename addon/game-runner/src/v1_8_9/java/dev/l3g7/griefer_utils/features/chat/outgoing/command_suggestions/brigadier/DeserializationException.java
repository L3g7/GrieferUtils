package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier;

/**
 * Thrown to indicate that a node could not be deserialized.
 * <p>
 * This should only happen due to outdated clients.
 */
public class DeserializationException extends RuntimeException {

	public DeserializationException(String message) {
		super(message);
	}

}
