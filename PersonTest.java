import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class PersonTest {

    private static final String TEST_FILE = "test_persons.txt";

    @BeforeEach
    public void setUp() throws IOException {
        // Create sample person in file
        String samplePerson = "56s_d%&fAB,John,Doe,32|Highland Street|Melbourne|Victoria|Australia,15-11-2000,false\n";
        Files.write(Paths.get(TEST_FILE), samplePerson.getBytes());
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Clean up test file
        Files.deleteIfExists(Paths.get(TEST_FILE));
    }

    @Test
    public void testUpdateDOBOnly() {
        Person person = new Person("56s_d%&fAB", "John", "Doe", "32|Highland Street|Melbourne|Victoria|Australia", "15-11-2000");

        // Should succeed: only changing DOB
        boolean result = person.updatePersonalDetails(
                "56s_d%&fAB", // same ID
                "John",       // same first name
                "Doe",        // same last name
                "32|Highland Street|Melbourne|Victoria|Australia", // same address
                "01-01-2001"  // new DOB
        );

        assertTrue(result);

        // Verify file contains updated birthdate
        String contents = readFile();
        assertTrue(contents.contains("01-01-2001"));
    }

    @Test
    public void testUpdateFailsWhenDOBAndOtherFieldsChange() {
        Person person = new Person("56s_d%&fAB", "John", "Doe", "32|Highland Street|Melbourne|Victoria|Australia", "15-11-2000");

        // Should fail: changing DOB + name
        boolean result = person.updatePersonalDetails(
                "56s_d%&fAB",
                "Jane", // changed name
                "Doe",
                "32|Highland Street|Melbourne|Victoria|Australia",
                "01-01-2001" // changed DOB
        );

        assertFalse(result);

        // Verify file did not change
        String contents = readFile();
        assertFalse(contents.contains("Jane"));
    }

    @Test
    public void testCannotChangeIDIfStartsWithEvenDigit() {
        Person person = new Person("56s_d%&fAB", "John", "Doe", "32|Highland Street|Melbourne|Victoria|Australia", "15-11-2000");

        // Starts with '5' (odd) — should allow
        boolean result = person.updatePersonalDetails(
                "76s_d%&fAB", // changed to a different ID starting with even digit (invalid)
                "John",
                "Doe",
                "32|Highland Street|Melbourne|Victoria|Australia",
                "15-11-2000"
        );

        assertFalse(result);
    }

    @Test
    public void testCannotChangeAddressIfUnder18() {
        Person person = new Person("56s_d%&fAB", "John", "Doe", "32|Highland Street|Melbourne|Victoria|Australia", "15-11-2010");

        // Change address for someone under 18 → should fail
        boolean result = person.updatePersonalDetails(
                "56s_d%&fAB",
                "John",
                "Doe",
                "10|New St|Melbourne|Victoria|Australia",
                "15-11-2010"
        );

        assertFalse(result);
    }

    @Test
    public void testSuccessfulUpdateWhenRulesAllow() {
        Person person = new Person("57x$#_aAB", "Alice", "Wong", "10|Apple Rd|Melbourne|Victoria|Australia", "15-11-2000");
        
        // Write initial line manually for this test
        String altPerson = "57x$#_aAB,Alice,Wong,10|Apple Rd|Melbourne|Victoria|Australia,15-11-2000,false\n";
        try {
            Files.write(Paths.get(TEST_FILE), altPerson.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            fail("Failed to prepare test file.");
        }

        // Person is adult, ID does not start with even digit, not changing DOB
        boolean result = person.updatePersonalDetails(
                "57x$#_aAB",
                "Alice",
                "Wong",
                "12|Banana Rd|Melbourne|Victoria|Australia",
                "15-11-2000"
        );

        assertTrue(result);
        assertTrue(readFile().contains("Banana Rd"));
    }

    private String readFile() {
        try {
            return Files.readString(Paths.get(TEST_FILE));
        } catch (IOException e) {
            return "";
        }
    }
}
