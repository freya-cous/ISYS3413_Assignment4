import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

public class PersonTest {

    private static final String TEST_FILE = "test_persons.txt";

    @BeforeEach
    public void setUp() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE));
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE));
    }

    // ------------------ addPerson() Tests ------------------

    @Test
    public void testValidAddPerson() {
        Person p = new Person("56s_d%&fAB", "John", "Doe", "32|Highland Street|Melbourne|Victoria|Australia", "15-11-1990");
        assertTrue(p.addPerson());
    }

    @Test
    public void testAddPersonInvalidID() {
        Person p = new Person("12abcXYZ!!", "Alice", "Smith", "12|Street|Melbourne|Victoria|Australia", "15-11-1990");
        assertFalse(p.addPerson());
    }

    @Test
    public void testAddPersonInvalidAddress() {
        Person p = new Person("56s_d%&fAB", "John", "Doe", "32|Street|City|NSW|Australia", "15-11-1990");
        assertFalse(p.addPerson());
    }

    @Test
    public void testAddPersonInvalidBirthdate() {
        Person p = new Person("56s_d%&fAB", "John", "Doe", "32|Street|Melbourne|Victoria|Australia", "1990-11-15");
        assertFalse(p.addPerson());
    }

    @Test
    public void testAddPersonMissingSpecialChars() {
        Person p = new Person("56abcdefAB", "John", "Doe", "32|Street|Melbourne|Victoria|Australia", "15-11-1990");
        assertFalse(p.addPerson());
    }

    // ------------------ updatePersonalDetails() Tests ------------------

    @Test
    public void testUpdateDOBOnly() throws IOException {
        writeSample("56s_d%&fAB,John,Doe,32|Street|Melbourne|Victoria|Australia,15-11-2000,false\n");
        Person p = new Person("56s_d%&fAB", "John", "Doe", "32|Street|Melbourne|Victoria|Australia", "15-11-2000");
        assertTrue(p.updatePersonalDetails("56s_d%&fAB", "John", "Doe", "32|Street|Melbourne|Victoria|Australia", "01-01-2001"));
    }

    @Test
    public void testUpdateDOBAndOtherFields() throws IOException {
        writeSample("56s_d%&fAB,John,Doe,32|Street|Melbourne|Victoria|Australia,15-11-2000,false\n");
        Person p = new Person("56s_d%&fAB", "John", "Doe", "32|Street|Melbourne|Victoria|Australia", "15-11-2000");
        assertFalse(p.updatePersonalDetails("56s_d%&fAB", "Jane", "Doe", "32|Street|Melbourne|Victoria|Australia", "01-01-2001"));
    }

    @Test
    public void testUpdateAddressUnder18() throws IOException {
        writeSample("56s_d%&fAB,John,Doe,32|Street|Melbourne|Victoria|Australia,15-11-2010,false\n");
        Person p = new Person("56s_d%&fAB", "John", "Doe", "32|Street|Melbourne|Victoria|Australia", "15-11-2010");
        assertFalse(p.updatePersonalDetails("56s_d%&fAB", "John", "Doe", "99|New|Melbourne|Victoria|Australia", "15-11-2010"));
    }

    @Test
    public void testUpdateIDStartsEven() throws IOException {
        writeSample("86s_d%&fAB,John,Doe,32|Street|Melbourne|Victoria|Australia,15-11-1990,false\n");
        Person p = new Person("86s_d%&fAB", "John", "Doe", "32|Street|Melbourne|Victoria|Australia", "15-11-1990");
        assertFalse(p.updatePersonalDetails("88s_d%&fAB", "John", "Doe", "32|Street|Melbourne|Victoria|Australia", "15-11-1990"));
    }

    @Test
    public void testUpdateAddressAllowed() throws IOException {
        writeSample("77s_d%&fAB,John,Doe,32|Street|Melbourne|Victoria|Australia,15-11-1990,false\n");
        Person p = new Person("77s_d%&fAB", "John", "Doe", "32|Street|Melbourne|Victoria|Australia", "15-11-1990");
        assertTrue(p.updatePersonalDetails("77s_d%&fAB", "John", "Doe", "90|Lane|Melbourne|Victoria|Australia", "15-11-1990"));
    }

    // ------------------ addDemeritPoints() Tests ------------------

    @Test
    public void testAddValidDemerit() {
        Person p = new Person("56s_d%&fAB", "John", "Doe", "Addr", "15-11-1990");
        assertEquals("Success", p.addDemeritPoints("01-01-2024", 3));
    }

    @Test
    public void testAddOver12PointsAdult() {
        Person p = new Person("56s_d%&fAB", "John", "Doe", "Addr", "15-11-1990");
        for (int i = 1; i <= 3; i++) {
            p.addDemeritPoints("01-0" + i + "-2024", 5);
        }
        assertTrue(p.addDemeritPoints("01-04-2024", 5).equals("Success"));
        assertTrue(p.isSuspended);
    }

    @Test
    public void testAddOldOffenseNoSuspension() {
        Person p = new Person("56s_d%&fAB", "John", "Doe", "Addr", "15-11-1990");
        assertEquals("Success", p.addDemeritPoints("01-01-2019", 6));
        assertFalse(p.isSuspended);
    }

    @Test
    public void testUnder21Over6PointsSuspends() {
        Person p = new Person("56s_d%&fAB", "John", "Doe", "Addr", "15-11-2008");
        p.addDemeritPoints("01-01-2024", 3);
        p.addDemeritPoints("01-02-2024", 4);
        assertTrue(p.isSuspended);
    }

    @Test
    public void testInvalidOffenseDate() {
        Person p = new Person("56s_d%&fAB", "John", "Doe", "Addr", "15-11-2000");
        assertEquals("Failed", p.addDemeritPoints("2024-01-01", 3));
    }

    private void writeSample(String content) throws IOException {
        Files.write(Paths.get(TEST_FILE), content.getBytes());
    }
}
