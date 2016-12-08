import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * ͼƬѹ��������
 */
public class BitmapCompressUtils {

    /**
     * ��СBitmap
     *
     * @param bitmap    ԭͼ
     * @param reqWidth  Ŀ����
     * @param reqHeight Ŀ��߶�
     * @return ��С���ͼƬ, �������ŷ���null
     */
    public static Bitmap scale(Bitmap bitmap, int reqWidth, int reqHeight) {
        Matrix matrix = new Matrix();
        float sx = (float) reqWidth / (float) bitmap.getWidth();
        float sy = (float) reqHeight / (float) bitmap.getHeight();

        //��������
        if (sx == 1f && sy == 1) {
            return null;
        }

        matrix.postScale(sx, sy);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * ѹ��ͼƬ��ָ���ļ���С<br/>
     * �㷨���ģ�<br/>
     * 1.��ͨ�������ļ���С��Ŀ���С�ı��������ųɺ���Ŀ��<br/>
     * 2.������������С����ͨ����ѭ�������ٲ������������ļ���С<br/>
     *
     * @param imgPath        ԭͼ·��
     * @param targetPath     Ŀ��·��
     * @param targetByte     Ŀ���С���ֽڣ�
     * @param sampleDescOnce ������һ�μ���������0-99����Ĭ��Ϊ10
     * @return �������Ҫѹ��������null�������Ҫѹ���򷵻�Ŀ��·��
     */
    public static String compress(String imgPath, String targetPath, long targetByte, int sampleDescOnce) {

        long fileLen = new File(imgPath).length();

        if (fileLen <= targetByte) {//ͼƬС��Ŀ���С����ѹ��
            return null;
        }

        //����ͼƬ���
        BitmapFactory.Options options = new BitmapFactory.Options();

        //���ú��������
        double scale = Math.sqrt((float) fileLen / targetByte);
        options.inSampleSize = (int) Math.ceil(scale);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;

        Bitmap originalBitmap = BitmapFactory.decodeFile(imgPath, options);

        //����ͼƬ�ĺ�����
        int fitHeight = (int) (originalBitmap.getHeight() / Math.sqrt(scale));
        int fitWidth = (int) (originalBitmap.getWidth() / Math.sqrt(scale));

        //�õ������ź��ͼƬ
        Bitmap bitmap = scale(originalBitmap, fitWidth, fitHeight);

        if (bitmap != null) {//����ԭͼ
            originalBitmap.recycle();
            //noinspection UnusedAssignment
            originalBitmap = null;
        } else {//ʹ��ԭͼ
            bitmap = originalBitmap;
        }

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        int rate = 100;

        do {
            bOut.reset();
            rate -= (sampleDescOnce <= 0 ? 10 : sampleDescOnce);//ѭ����������,Ĭ��Ϊ10
            if (rate < 0) {//�����ٵ��ˣ�ѹ������
                break;
            }

            //��һ������ ��ͼƬ��ʽ
            //�ڶ��������� ͼƬ������100Ϊ��ߣ�0Ϊ���
            //����������������ѹ��������ݵ���
            bitmap.compress(Bitmap.CompressFormat.JPEG, rate, bOut);

        }
        while (bOut.size() > targetByte); //���ѹ�������Ŀ���С�������ѹ���ʣ�����ѹ��
        
        //����ͼƬ
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, rate, new FileOutputStream(targetPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return targetPath;
    }


}
