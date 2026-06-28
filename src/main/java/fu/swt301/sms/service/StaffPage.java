package fu.swt301.sms.service;

import fu.swt301.sms.entity.Staff;

import java.util.List;

public class StaffPage {
    private final List<Staff> staffList;
    private final int page;
    private final int pageSize;
    private final int totalItems;
    private final int totalPages;

    public StaffPage(List<Staff> staffList, int page, int pageSize, int totalItems) {
        this.staffList = staffList;
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
    }

    public List<Staff> getStaffList() {
        return staffList;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
