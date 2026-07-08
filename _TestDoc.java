import com.healthsys.dao.DoctorDAO;
import com.healthsys.common.entity.Doctor;
public class _TestDoc {
    public static void main(String[] args) {
        DoctorDAO dao = new DoctorDAO();
        Doctor d = dao.getByUsername("d001");
        if (d != null) {
            System.out.println("FOUND: id=" + d.getDoctorId() + " name=" + d.getName() + " pwd=" + d.getPasswordHash());
        } else {
            System.out.println("NOT FOUND");
        }
        System.out.println("Count: " + dao.getAll().size());
    }
}