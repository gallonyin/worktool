package org.yameida.worktool.model;

import java.io.Serializable;
import java.util.Date;

public class AppUpdate implements Serializable {
    private Long id;

//    @ApiModelProperty(value = "应用名称")
    private String appName;

//    @ApiModelProperty(value = "更新标题")
    private String title;

//    @ApiModelProperty(value = "更新日志")
    private String updateLog;

//    @ApiModelProperty(value = "备注")
    private String remark;

//    @ApiModelProperty(value = "更新版本号")
    private String versionName;

//    @ApiModelProperty(value = "内部版本号")
    private Integer versionCode;

//    @ApiModelProperty(value = "强制更新内部版本号")
    private Integer minVersionCode;

//    @ApiModelProperty(value = "apk链接")
    private String downloadUrl;

//    @ApiModelProperty(value = "创建时间")
    private Date createTime;

//    @ApiModelProperty(value = "安装包大小")
    private String size;

//    @ApiModelProperty(value = "可用")
    private Boolean enable;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdateLog() {
        return updateLog;
    }

    public void setUpdateLog(String updateLog) {
        this.updateLog = updateLog;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public Integer getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public Integer getMinVersionCode() {
        return minVersionCode;
    }

    public void setMinVersionCode(Integer minVersionCode) {
        this.minVersionCode = minVersionCode;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", appName=").append(appName);
        sb.append(", title=").append(title);
        sb.append(", updateLog=").append(updateLog);
        sb.append(", remark=").append(remark);
        sb.append(", versionName=").append(versionName);
        sb.append(", versionCode=").append(versionCode);
        sb.append(", minVersionCode=").append(minVersionCode);
        sb.append(", downloadUrl=").append(downloadUrl);
        sb.append(", createTime=").append(createTime);
        sb.append(", size=").append(size);
        sb.append(", enable=").append(enable);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}