package com.itc.integration.pojo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageBean<T> {
    /**
     * 页码
     */
    private int pageNum;
    /**
     * 页大小
     */
    private int pageSize;
    /**
     * 数据总数
     */
    private long totalSize;
    /**
     * 总页数
     */
    private int pageCount;
    /**
     * 页面数据
     */
    private List<T> rows;

    public PageBean(int pageNum, int pageSize, long totalSize, List<T> rows) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalSize = totalSize;
        pageCount = (int) (totalSize % pageSize == 0 ? totalSize / pageSize : totalSize / pageSize + 1);
        this.rows = rows;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        return "PageBean{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", totalSize=" + totalSize +
                ", pageCount=" + pageCount +
                ", rows=" + rows +
                '}';
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("pageNum", pageNum);
        map.put("pageSize", pageSize);
        map.put("totalSize", totalSize);
        map.put("pageCount", pageCount);
        map.put("rows", rows);
        return map;
    }
}
