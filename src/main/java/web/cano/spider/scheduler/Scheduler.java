package web.cano.spider.scheduler;

import web.cano.spider.Page;
import web.cano.spider.Task;

import java.util.List;

/**
 * Scheduler is the part of url management.<br>
 * You can implement interface Scheduler to do:
 * manage urls to fetch
 * remove duplicate urls
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
public interface Scheduler {

    /**
     * add a url to fetch
     *
     * @param page
     * @param task
     */
    public void push(Page page, Task task);

    /**
     * 不管是否重复都加到队列
     * @param page
     * @param task
     */
    public void forcePush(Page page, Task task);

    /**
     * add a url to fetch to queue head
     *
     * @param page
     * @param task
     */
    public void pushToHead(Page page, Task task);

    /**
     * get an url to crawl
     *
     * @param task the task of spider
     * @return the url to crawl
     */
    public Page poll(Task task);

    /**
     * 标志一个request or task执行完毕
     * @param task
     */
    public void completeParse(Page page, Task task);

    /**
     * 判断是否所有的pages都执行完毕
     */
    public List<Page> checkIfCompleteParse(Task task);

    /**
     * save to queue to db
     */
    public void saveUrlSet(Task task);

    /**
     * recover queue from db
     * @param task
     */
    public void recoverUrlSet(Task task);

}
