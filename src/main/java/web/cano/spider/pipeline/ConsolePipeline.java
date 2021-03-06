package web.cano.spider.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.*;

import java.util.List;

/**
 * Write results in console.<br>
 * Usually used in test.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
public class ConsolePipeline implements Pipeline {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void process(Page page, Task task) {
        if (page.isSkip() || page.isResource()) return;

        Spider spider = (Spider) task;
        List<PageItem> pageItems = page.getPageItems();

        boolean isMultiple = false;
        int multiNumber = 1;
        for(PageItem item : pageItems){
            if(item.isMultiple()){
                isMultiple = true;
                List<String> list = (List<String>) item.getItemValue();
                multiNumber = list.size();
                break;
            }
        }

        if(isMultiple && spider.getSite().isShouldSplitToMultipleValues()){
            printAsMultipleRecords(page,multiNumber);
        }else {
            String separator = spider.getSite().getMultiValueSeparator();
            printAsSingleRecord(page,separator);
        }
    }

    private void printAsSingleRecord(Page page, String separator){
        List<PageItem> pageItems = page.getPageItems();
        StringBuilder sb = new StringBuilder();
        sb.append("get page: " + page.getUrl() + "\n");
        for (PageItem item : pageItems) {
            if(item.isMultiple()){
                List<String> list = (List<String>) item.getItemValue();
                if(list == null || list.size() == 0){
                    sb.append("");
                }else {
                    sb.append(item.getItemName() + ":\t" + list.get(0));
                    for (int i = 1; i < list.size(); i++) {
                        sb.append(separator + list.get(i));
                    }
                }
                sb.append("\n");
            }else {
                Object value = item.getItemValue() == null ? "" : item.getItemValue();
                sb.append(item.getItemName() + ":\t" + value + "\n");
            }
        }
        logger.info(sb.toString());
    }

    private void printAsMultipleRecords(Page page,int multiNumber){
        List<PageItem> pageItems = page.getPageItems();

        StringBuilder sb = new StringBuilder();
        sb.append("get page: " + page.getRequest().getUrl() + "\n");
        for(int i=0; i<multiNumber; i++) {
            for (PageItem item : pageItems) {
                if(item.isMultiple()){
                    List<String> list = (List<String>) item.getItemValue();
                    sb.append(item.getItemName() + ":\t" + list.get(i) + "\n");
                }else {
                    sb.append(item.getItemName() + ":\t" + item.getItemValue().toString() + "\n");
                }
            }
            sb.append("\n");
        }
        logger.info(sb.toString());
    }
}
