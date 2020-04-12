package com.atguigu.gulimall.thirdparty;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Autowired(required = false)
    private OSSClient ossClient;

    @Test
    void contextLoads() {
    }
   @Test
    public void testUpload() {
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = "http://oss-cn-shanghai.aliyuncs.com";
        // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = "LTAI4FebH6puhuUSEFzmTF6o";
        String accessKeySecret = "LtdMelM8abzCZPb1OIplpPgnG7IQUO";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 创建PutObjectRequest对象。
        PutObjectRequest putObjectRequest = new PutObjectRequest("gulimall-zlc", "rainy", new File("C:\\Users\\QAQAQA\\Pictures\\69b625c455d52cb1c59573109ce8c250ba4a64b4.jpg"));

        // 如果需要上传时设置存储类型与访问权限，请参考以下示例代码。
        // ObjectMetadata metadata = new ObjectMetadata();
        // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
        // metadata.setObjectAcl(CannedAccessControlList.Private);
        // putObjectRequest.setMetadata(metadata);

        // 上传文件。
        ossClient.putObject(putObjectRequest);

        // 关闭OSSClient。
        ossClient.shutdown();

        System.out.println("上传成功...");
    }

    @Test
    public void testUpload02() throws FileNotFoundException {
        ossClient.putObject("gulimall-zlc", "BUGImage.jpg", new FileInputStream("C:\\Users\\QAQAQA\\Pictures\\6d6f2275d27e12ddf9deac2fd47a511344c9125d.png"));
        System.out.println("上传成功...");
    }
}
