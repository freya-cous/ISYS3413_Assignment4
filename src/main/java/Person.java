import java.io.*;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Person {
    private String personID;
    private String firstName;
    private String lastName;
    private String address;
    private String birthdate;
    private HashMap<String, Integer> demeritPoints = new HashMap<>();
    private boolean isSuspended = false;

    private static final String FILE_NAME = "test_persons.txt";

    public Person(String personID, String firstName, String lastName, String address, String birthdate) {
        this.personID = personID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.birthdate = birthdate;
    }

    public boolean addPerson() {
        if (!isValidID(this.personID) || !isValidAddress(this.address) || !isValidDate(this.birthdate)) return false;
        try (FileWriter fw = new FileWriter(FILE_NAME, true)) {
            fw.write(this.toString() + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean updatePersonalDetails(String newID, String newFirstName, String newLastName, String newAddress, String newBirthdate) {
        File file = new File(FILE_NAME);
        if (!file.exists()) return false;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            List<String> updatedLines = new ArrayList<>();
            boolean updated = false;

            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length < 6) {
                    updatedLines.add(line);
                    continue;
                }

                String currentID = parts[0];
                if (!currentID.equals(this.personID)) {
                    updatedLines.add(line);
                    continue;
                }

                Person oldPerson = new Person(parts[0], parts[1], parts[2], parts[3], parts[4]);
                boolean isChangingDOB = !oldPerson.birthdate.equals(newBirthdate);
                boolean isUnder18 = getAge(newBirthdate) < 18;
                boolean isEvenStart = Character.getNumericValue(oldPerson.personID.charAt(0)) % 2 == 0;

                if (isChangingDOB &&
                   (!oldPerson.firstName.equals(newFirstName) ||
                    !oldPerson.lastName.equals(newLastName) ||
                    !oldPerson.address.equals(newAddress) ||
                    !oldPerson.personID.equals(newID))) {
                    return false;
                }

                if (!isChangingDOB && isUnder18 && !oldPerson.address.equals(newAddress)) {
                    return false;
                }

                if (!isChangingDOB && isEvenStart && !oldPerson.personID.equals(newID)) {
                    return false;
                }

                Person updatedPerson = new Person(newID, newFirstName, newLastName, newAddress, newBirthdate);
                if (!updatedPerson.isValidID(newID) || !updatedPerson.isValidAddress(newAddress) || !updatedPerson.isValidDate(newBirthdate)) {
                    return false;
                }

                updatedLines.add(updatedPerson.toString());
                updated = true;
            }

            if (updated) Files.write(file.toPath(), updatedLines);
            return updated;
        } catch (IOException e) {
            return false;
        }
    }

    public String addDemeritPoints(String offenseDate, int points) {
        if (!isValidDate(offenseDate) || points < 1 || points > 6) return "Failed";

        demeritPoints.put(offenseDate, points);
        int age = getAge(this.birthdate);
        int totalPoints = demeritPoints.entrySet().stream()
                .filter(e -> withinTwoYears(e.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();

        if ((age < 21 && totalPoints > 6) || (age >= 21 && totalPoints > 12)) {
            this.isSuspended = true;
        }

        return "Success";
    }

    public boolean isSuspended() {
        return isSuspended;
    }

    private boolean isValidID(String id) {
        if (id == null || id.length() != 10) return false;
        if (!id.substring(0, 2).matches("[2-9]{2}")) return false;
        if (!id.substring(2, 8).replaceAll("[^\\W_]", "").matches(".*\\W.*\\W.*")) return false;
        return id.substring(8).matches("[A-Z]{2}");
    }

    private boolean isValidAddress(String addr) {
        String[] parts = addr.split("\\|");
        return parts.length == 5 && parts[3].equalsIgnoreCase("Victoria");
    }

    private boolean isValidDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            sdf.setLenient(false); // ← Important: don't allow flexible parsing
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    
    private int getAge(String birthdateStr) {
        try {
            Date birth = new SimpleDateFormat("dd-MM-yyyy").parse(birthdateStr);
            Calendar dob = Calendar.getInstance();
            dob.setTime(birth);
            Calendar now = Calendar.getInstance();
            int age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (now.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--;
            return age;
        } catch (ParseException e) {
            return 0;
        }
    }

    private boolean withinTwoYears(String dateStr) {
        try {
            Date offenseDate = new SimpleDateFormat("dd-MM-yyyy").parse(dateStr);
            Calendar limit = Calendar.getInstance();
            limit.add(Calendar.YEAR, -2);
            return offenseDate.after(limit.getTime());
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.join(",", personID, firstName, lastName, address, birthdate, String.valueOf(isSuspended));
    }
}
