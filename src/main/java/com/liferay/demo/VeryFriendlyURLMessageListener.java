package com.liferay.demo;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalFolder;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.PortalUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

@Component(
        immediate=true,property=("destination.name=" + VeryFriendlyURLConfigurator.DESTINATION),
        service = MessageListener.class
)
public class VeryFriendlyURLMessageListener implements MessageListener {

    private long WAITTIME = 1000;

    @Override
    public void receive(Message message) {

        try {
            _log.debug("Let's wait " + WAITTIME + " milliseconds..");
            Thread.sleep(WAITTIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean isModified = false;

        JournalArticle article;

        try {
            long groupid = (long) message.get("groupId");
            Locale defaultLocale = PortalUtil.getSiteDefaultLocale(groupid);

            article = _JournalArticleLocalService.getArticle(groupid, (String) message.get("articleId"));

            ServiceContext serviceContext = new ServiceContext();
            serviceContext.setCompanyId(article.getCompanyId());
            serviceContext.setScopeGroupId(article.getGroupId());

            String path = getFolderPath(article.getFolder(), "");

            if (!path.isEmpty() && !path.equalsIgnoreCase("/")) {

                Map<Locale, String> friendlyURLMap = null;

                try {

                    friendlyURLMap = article.getFriendlyURLMap();

                    if (!friendlyURLMap.isEmpty()) {
                        // using for-each loop for iteration over Map.entrySet()
                        for (Map.Entry<Locale, String> entry : friendlyURLMap.entrySet()) {
                            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                            if (entry.getValue().lastIndexOf(path)<0) {
                                isModified = true;
                                entry.setValue(path + entry.getValue().substring(entry.getValue().lastIndexOf("/") + 1));
                            }
                        }
                    }
                } catch (PortalException e) {
                    e.printStackTrace();
                }

                if (isModified) {
                    _JournalArticleLocalService.updateArticle(
                            article.getUserId(),
                            article.getGroupId(),
                            article.getFolderId(),
                            article.getArticleId(),
                            article.getVersion(),
                            article.getTitleMap(),
                            article.getDescriptionMap(),
                            friendlyURLMap,
                            article.getContent(),
                            article.getDDMStructureKey(),
                            article.getDDMTemplateKey(),
                            article.getLayoutUuid(),
                            this.getMonth(article.getDisplayDate()),
                            this.getDay(article.getDisplayDate()),
                            this.getYear(article.getDisplayDate()),
                            this.getHours(article.getDisplayDate()),
                            this.getMinutes(article.getDisplayDate()),
                            this.getMonth(article.getExpirationDate()),
                            this.getDay(article.getExpirationDate()),
                            this.getYear(article.getExpirationDate()),
                            this.getHours(article.getExpirationDate()),
                            this.getMinutes(article.getExpirationDate()),
                            !isDateSet(article.getExpirationDate()),
                            this.getMonth(article.getReviewDate()),
                            this.getDay(article.getReviewDate()),
                            this.getYear(article.getReviewDate()),
                            this.getHours(article.getReviewDate()),
                            this.getMinutes(article.getReviewDate()),
                            !isDateSet(article.getReviewDate()),
                            article.getIndexable(),
                            article.getSmallImage(),
                            article.getSmallImageURL(),
                            null,
                            null,
                            article.getUrlTitle(),
                            serviceContext
                    );
                }
            }

        } catch (PortalException e) {
            _log.error(e.getMessage());
        }
    }

    private String getFolderPath(JournalFolder folder, String path) throws PortalException {

        path = folder.getName() + "/" + path;

        if (folder.isRoot()) {
            _log.info("isRoot: " + folder.getName());
            _log.info("path: " + path);
            return path;
        } else {
            return getFolderPath(folder.getParentFolder(),path);
        }
    }

    private boolean isDateSet(Date date)
    {
        if (date == null || date.getYear() == 0) {
            return false;
        } else {
            return true;
        }
    }

    private int getMonth(Date date)
    {
        if (date == null) {
            return 0;
        } else {
            return date.getMonth();
        }
    }

    private int getDay(Date date)
    {
        if (date == null) {
            return 0;
        } else {
            return date.getDay();
        }
    }

    private int getYear(Date date)
    {
        if (date == null) {
            return 0;
        } else {
            return date.getYear();
        }
    }

    private int getHours(Date date)
    {
        if (date == null) {
            return 0;
        } else {
            return date.getHours();
        }
    }

    private int getMinutes(Date date)
    {
        if (date == null) {
            return 0;
        } else {
            return date.getMinutes();
        }
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected JournalArticleLocalService _JournalArticleLocalService;

    private static final Log _log = LogFactoryUtil.getLog(VeryFriendlyURLMessageListener.class);
}
