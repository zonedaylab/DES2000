package cn.zup.iot.timerdecision.util.file;

import java.io.File;

public class DirUtil {

    final private static Object LockRW = new Object();

    /***************************************************************************
     * ���ܣ����ϵͳĿ¼�������ھʹ��� ����paths,·�������磺/home/ies/ies600/
     **************************************************************************/
    public boolean checkSysDir(String paths) {
        boolean r_ = false;

        try {
            synchronized (LockRW) {
                String[] p = paths.split("/");
                String tpath = "/";
                for (int i = 0; i < p.length; i++) {
                    if (p[i].length() <= 0) {
                        continue;
                    }
                    tpath = tpath + p[i];
                    File fp = new File(tpath);
                    if (fp.exists()) { // ����ļ�����

                        if (fp.isFile()) { // ������ļ���Ҫ�ٴ���ͬ���Ŀ¼

                            if (!fp.mkdir()) {
                                return r_;
                            }
                        }
                    } else { // ������

                        if (!fp.mkdir()) {
                            return r_;
                        }
                    }

                    tpath = tpath + "/";
                }

                r_ = true;
            }
        } catch (Exception e) {
            //
        }

        return r_;
    }

    /***************************************************************************
     * ���ܣ�����ļ�·���Ƿ���ڣ������ڲ����� ����filepath,·�������磺/home/ies/ies600/my.txt
     **************************************************************************/
    public boolean checkPath(String filepath) {
        boolean r_ = false;
        if ((filepath == null) || (filepath.length() == 0)) {
            return r_;
        }

        try {
            File fp = new File(filepath);
            r_ = fp.exists();
        } catch (Exception e) {
            //
        }

        return r_;
    }

}
