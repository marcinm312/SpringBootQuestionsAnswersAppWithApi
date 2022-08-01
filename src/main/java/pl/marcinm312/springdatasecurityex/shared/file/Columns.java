package pl.marcinm312.springdatasecurityex.shared.file;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class Columns {

	static final String ID_COLUMN = "Id";
	static final String ANSWER_TEXT_COLUMN = "Treść odpowiedzi";
	static final String CREATION_DATE_COLUMN = "Data utworzenia";
	static final String MODIFICATION_DATE_COLUMN = "Data modyfikacji";
	static final String USER_COLUMN = "Użytkownik";
	static final String QUESTION_TITLE_COLUMN = "Tytuł";
	static final String QUESTION_DESCRIPTION_COLUMN = "Opis";
}
