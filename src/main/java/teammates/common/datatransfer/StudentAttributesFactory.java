package teammates.common.datatransfer;

import teammates.common.exception.EnrollException;
import teammates.common.util.Assumption;
import teammates.common.util.Const;
import teammates.common.util.FieldValidator;
import teammates.common.util.StringHelper;

/**
 * A factory for the creation of {@link StudentAttributes} objects.
 * It encapsulates the mechanism to create a {@link StudentAttributes} object
 * from a given enroll line.
 */
public class StudentAttributesFactory {
    public static final int MIN_FIELD_COUNT = 3;
    public static final int MAX_FIELD_COUNT = 5;

    public static final String ERROR_HEADER_ROW_FIELD_REPEATED = "The header row contains repeated fields";
    public static final String ERROR_HEADER_ROW_FIELD_MISSED =
            "The following required column names are missing in the header row";

    public static final String ERROR_ENROLL_LINE_EMPTY = "Enroll line was empty\n";
    public static final String ERROR_ENROLL_LINE_TOOFEWPARTS =
            "Enroll line had fewer than the minimally expected "
            + MIN_FIELD_COUNT + " columns (Team, Name and Email)\n";

    public static final int FIRST_COLUMN_INDEX = 0;
    public static final int SECOND_COLUMN_INDEX = 1;
    public static final int THIRD_COLUMN_INDEX = 2;
    public static final int FOURTH_COLUMN_INDEX = 3;
    public static final int FIFTH_COLUMN_INDEX = 4;

    private int sectionColumnIndex;
    private int teamColumnIndex;
    private int nameColumnIndex;
    private int emailColumnIndex;
    private int commentColumnIndex;

    private boolean hasSection;
    private boolean hasTeam;
    private boolean hasName;
    private boolean hasEmail;
    private boolean hasComment;

    public StudentAttributesFactory() throws EnrollException {
        this("section|team|name|email|comment");
    }

    /**
     * Construct a StudentAttributesFactory object with the given header row.<br>
     * The column names allowed in header row: {team, name, email, comment}<br>
     * They are case insensitive and plural nouns are allowed. If the header row
     * doesn't contain all required columns, then it is ignored and treated as a
     * normal enroll line. <br>
     * Pre-condition: headerRow must not be null
     *
     * @throws EnrollException
     */
    public StudentAttributesFactory(String headerRow) throws EnrollException {
        Assumption.assertNotNull(headerRow);

        int fieldCount = locateColumnIndexes(headerRow);

        if (fieldCount < MIN_FIELD_COUNT || !hasTeam || !hasName || !hasEmail) {
            String missingField = "";
            missingField = hasTeam ? missingField : missingField + " <mark>Team</mark>";
            missingField = hasName ? missingField : missingField + " <mark>Name</mark>";
            missingField = hasEmail ? missingField : missingField + " <mark>Email</mark>";
            throw new EnrollException(ERROR_HEADER_ROW_FIELD_MISSED + ": " + missingField);
        }
    }

    /**
     * Return a StudentAttributes object created from the given enrollLine.
     *
     * @throws EnrollException
     */
    public StudentAttributes makeStudent(String enrollLine, String courseId) throws EnrollException {
        if (enrollLine.isEmpty()) {
            throw new EnrollException(ERROR_ENROLL_LINE_EMPTY);
        }

        String[] columns = splitLineIntoColumns(enrollLine);

        boolean hasMissingFields = columns.length <= teamColumnIndex
                                   || columns.length <= nameColumnIndex
                                   || columns.length <= emailColumnIndex;

        if (hasMissingFields) {
            throw new EnrollException(ERROR_ENROLL_LINE_TOOFEWPARTS);
        }

        String paramTeam = columns[teamColumnIndex].trim();
        String paramName = columns[nameColumnIndex].trim();
        String paramEmail = columns[emailColumnIndex].trim();

        String paramComment = "";

        if (hasComment && columns.length > commentColumnIndex) {
            paramComment = columns[commentColumnIndex].trim();
        }

        String paramSection;

        if (hasSection && columns.length > sectionColumnIndex) {
            paramSection = columns[sectionColumnIndex].trim();
        } else {
            paramSection = Const.DEFAULT_SECTION;
        }

        return new StudentAttributes(paramSection, paramTeam, paramName, paramEmail, paramComment, courseId);
    }

    private int locateColumnIndexes(String headerRow) throws EnrollException {
        int fieldCount = 0;
        int count = 0;

        hasSection = false;
        hasTeam = false;
        hasName = false;
        hasEmail = false;
        hasComment = false;

        String[] columns = splitLineIntoColumns(headerRow);

        for (int curPos = 0; curPos < columns.length; curPos++) {
            String str = columns[curPos].trim().toLowerCase();

            if (StringHelper.isAnyMatching(str, FieldValidator.REGEX_COLUMN_SECTION)) {
                sectionColumnIndex = curPos;
                count++;
                fieldCount = (!hasSection) ? fieldCount + 1 : fieldCount;
                hasSection = true;
            } else if (StringHelper.isAnyMatching(str, FieldValidator.REGEX_COLUMN_TEAM)) {
                teamColumnIndex = curPos;
                count++;
                fieldCount = (!hasTeam) ? fieldCount + 1 : fieldCount;
                hasTeam = true;
            } else if (StringHelper.isAnyMatching(str, FieldValidator.REGEX_COLUMN_NAME)) {
                nameColumnIndex = curPos;
                count++;
                fieldCount = (!hasName) ? fieldCount + 1 : fieldCount;
                hasName = true;
            } else if (StringHelper.isAnyMatching(str, FieldValidator.REGEX_COLUMN_EMAIL)) {
                emailColumnIndex = curPos;
                count++;
                fieldCount = (!hasEmail) ? fieldCount + 1 : fieldCount;
                hasEmail = true;
            } else if (StringHelper.isAnyMatching(str, FieldValidator.REGEX_COLUMN_COMMENT)) {
                commentColumnIndex = curPos;
                count++;
                fieldCount = (!hasComment) ? fieldCount + 1 : fieldCount;
                hasComment = true;
            } else {
                // do nothing as it is a empty column
            }
        }

        if (count > fieldCount) {
            throw new EnrollException(ERROR_HEADER_ROW_FIELD_REPEATED);
        }

        return fieldCount;
    }

    private String[] splitLineIntoColumns(String line) {
        Assumption.assertNotNull(line);
        String[] cols = line.replace("|", "\t").split("\t", -1);
        return cols;
    }
}
