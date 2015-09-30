package web.cano.spider.pipeline;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.provider.ConfigFile;
import web.cano.spider.*;

import java.io.*;
import java.util.List;

/**
 * Write results in console.<br>
 * Usually used in test.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
public class SaveSourceFilePipeline implements Pipeline {
    Logger logger = LoggerFactory.getLogger(getClass());

    private String filePath;

    private SaveSourceFilePipeline() {
    }

    public SaveSourceFilePipeline(String filePath) {
        this.filePath = filePath;

        try {
            FileUtils.forceMkdir(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(Page page, Task task) {
        if (page.isSkip()) return;

        String rawText = page.getRawText();
        String url = page.getUrl();
        String fileName = url.replace(".","").replace("/","").replace(":","").replace("?","").replace("&","").replace("-", "");
        if(fileName.length() >=300){
            fileName = fileName.substring(fileName.length()-300,fileName.length());
        }

        saveFile(fileName,rawText);
    }

    private void saveFile(String fileName, String content){
        try {
            File storeFile = new File(filePath + fileName);
            FileOutputStream output = FileUtils.openOutputStream(storeFile);
            output.write(content.getBytes());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ;
        }
    }
}
