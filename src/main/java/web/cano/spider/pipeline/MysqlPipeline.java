package web.cano.spider.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.*;
import web.cano.spider.utils.BaseDAO;

import java.util.List;

/**
 * 用于存储内容到mysql
 */
public class MysqlPipeline implements Pipeline {

    public static enum STATUS {Success,Failure,NotStarted}

    private STATUS status = STATUS.NotStarted;
    private BaseDAO dao = BaseDAO.getInstance("canospider");
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //是否清空数据库
    private boolean shouldResetDb = false;

    private MysqlPipeline(){}

    public MysqlPipeline(boolean shouldResetDb){
        this(shouldResetDb,false);
    }

    public MysqlPipeline(boolean shouldResetDb,boolean shouldSplitMultiContent){
        this.shouldResetDb = shouldResetDb;
    }

    @Override
    public void process(Page page, Task task) {
        if(page.isSkip() || page.isResource()) return;

        if(status == STATUS.Failure){
            logger.error("not able to create db table,stop processing");
            return;
        }

        String tableName = ((Spider)task).getUUID();
        if(status == STATUS.NotStarted){
            if(createTable(page,tableName)) {
                status = STATUS.Success;
            }else{
                status = STATUS.Failure;
                logger.error("create db table fails");
                return ;
            }
        }

        //insert to db
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

        if (isMultiple && spider.getSite().isShouldSplitToMultipleValues()) {
            insertRecordsToDb(page,tableName,multiNumber);
        } else {
            insertRecordToDb(page, tableName);
        }
    }

    private String itemValueMysqlString(PageItem.PageItemType type, Object value){
        switch (type){
            case INT:
                int intValue = Integer.parseInt(value.toString().trim());
                return ", " + value;
            case TEXT:
                String textValue = value == null ? "" : value.toString().trim().replace("'","\\'");
                return ", '" + textValue + "'";
            case DATE:
                String dateValue = value == null ? "1000-01-01" : value.toString().trim();
                return ", '" + dateValue + "'";
            case STRING:
                String strValue = value == null ? "" : value.toString().trim().replace("'", "\\'");
                return ", '" + strValue + "'";
        }
        return "";
    }

    private void insertRecordToDb(Page page, String tableName){
        String sql = "INSERT DELAYED INTO `" + tableName + "` (";
        String keys = "`id`";
        String values = "NULL";

        for(PageItem item : page.getPageItems()){
            keys = keys + ", `" + item.getItemName() + "`";
            values = values + itemValueMysqlString(item.getItemType(),item.getItemValue());
        }
        sql = sql + keys + ") VALUES (" + values + ");";
        logger.info(sql);
        dao.executeUpdate(sql);
    }

    private void insertRecordsToDb(Page page, String tableName,int multiNumber){
        List<PageItem> pageItems = page.getPageItems();

        String keys = "`id`";
        String values = "";
        for(int i=0; i<multiNumber; i++) {
            String value = "NULL";
            for (PageItem item : pageItems) {
                if(item.isMultiple()){
                    List<String> list = (List<String>) item.getItemValue();
                    if(i==0) keys = keys + ", `" + item.getItemName() + "`";
                    value = value + itemValueMysqlString(item.getItemType(),list.get(i));
                }else {
                    if(i==0) keys = keys + ", `" + item.getItemName() + "`";
                    value = value + itemValueMysqlString(item.getItemType(),item.getItemValue());
                }
            }
            if(i==0) {
                values = values + "(" + value + ")";
            }else{
                values = values + ",\n(" + value + ")";
            }
        }

        String sql = "INSERT DELAYED INTO `" + tableName + "` (";
        sql = sql + keys + ") VALUES " + values + ";";
        logger.info(sql);
        dao.executeUpdate(sql);
    }

    /**
     * create table with the definition of class
     * @return
     */
    private boolean createTable(Page page, String tableName) {
        //Map<String, PageItem.PageItemType> fields = page.getPageItems().getAllFields();

        if(this.shouldResetDb){
            String sql = "DROP TABLE IF EXISTS `" + tableName + "`;";
            dao.executeUpdate(sql);
            logger.info("drop table " + tableName + " and re-recreate again.");
        }

        logger.info("creating table " + tableName);
        String sql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (`id` int(11) NOT NULL AUTO_INCREMENT";
        List<PageItem> pageItems = page.getPageItems();
        for (PageItem item : pageItems) {
            sql = sql + ", `" + item.getItemName() + "` " + getItemTypeString(item.getItemType()) + " NULL";
        }
        sql = sql + ", PRIMARY KEY (`id`)) ENGINE=myisam;";
        logger.info(sql);
        dao.executeUpdate(sql);

        logger.info("create table " + tableName + " successfully");
        return true;
    }

    private String getItemTypeString(PageItem.PageItemType itemType){
            switch (itemType){
                case STRING:{
                    return "varchar(1000)";
                }
                case INT: {
                    return "int(11)";
                }
                case DATE:{
                    return "datetime";
                }
                case TEXT:{
                    return "text";
                }
                default:{
                    return "varchar(1000)";
                }
            }
    }
}
