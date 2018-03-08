package org.springframework.statemachine.dsl.ssml.assist;

public enum CasePreference {
	/**
	 * Suggest both uppercase and lowercase completions
	 */
    BOTH,

    /**
     * In case both uppercase and lowercase are supported by the grammar, suggest only lowercase alternative
     */
    LOWER,

    /**
     * In case both uppercase and lowercase are supported by the grammar, suggest only uppercase alternative
     */
    UPPER
}
