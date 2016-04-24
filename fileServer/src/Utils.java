import java.io.File;
import java.util.ArrayList;

/**
 * Created by michaelleung on 17/4/16.
 */
public class Utils {

    public static ArrayList<FileList> folder(File directory) {
        ArrayList<FileList> fileData = new ArrayList<FileList>();

        long length = 0;
        for (File file : directory.listFiles()) {
            FileList item = new FileList();
            item.setFileName(file.getName());

            if (file.isFile()) {
                length += file.length();
                item.setFileSize(length);
            }
            else{
                length = length + folderSize(file);
                item.setFileSize(length);
            }
            fileData.add(item);
        }
        return fileData;
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }
}
