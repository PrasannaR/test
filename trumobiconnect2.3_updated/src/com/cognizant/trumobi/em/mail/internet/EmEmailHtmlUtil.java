

package com.cognizant.trumobi.em.mail.internet;

import com.cognizant.trumobi.em.mail.EmMessagingException;
import com.cognizant.trumobi.em.mail.EmMultipart;
import com.cognizant.trumobi.em.mail.EmPart;
import com.cognizant.trumobi.em.mail.store.EmLocalStore.LocalAttachmentBodyPart;
import com.cognizant.trumobi.em.provider.EmAttachmentProvider;

import android.content.ContentResolver;
import android.net.Uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmEmailHtmlUtil {

    // Regex that matches characters that have special meaning in HTML. '<', '>', '&' and
    // multiple continuous spaces.
    private static final Pattern PLAIN_TEXT_TO_ESCAPE = Pattern.compile("[<>&]| {2,}|\r?\n");

    //TODO: make resolveInlineImage() work in the new content provider model.
    /**
     * Resolve content-id reference in src attribute of img tag to AttachmentProvider's
     * content uri.  This method calls itself recursively at most the number of
     * LocalAttachmentPart that mime type is image and has content id.
     * The attribute src="cid:content_id" is resolved as src="content://...".
     * This method is package scope for testing purpose.
     *
     * @param text html email text
     * @param part mime part which may contain inline image
     * @return html text in which src attribute of img tag may be replaced with content uri
     */
    public static String resolveInlineImage(
            ContentResolver resolver, long accountId, String text, EmPart part, int depth)
        throws EmMessagingException {
        // avoid too deep recursive call.
        if (depth >= 10 || text == null) {
            return text;
        }
        String contentType = EmMimeUtility.unfoldAndDecode(part.getContentType());
        String contentId = part.getContentId();
        if (contentType.startsWith("image/") &&
            contentId != null &&
            part instanceof LocalAttachmentBodyPart) {
            LocalAttachmentBodyPart attachment = (LocalAttachmentBodyPart)part;
            Uri attachmentUri =
                EmAttachmentProvider.getAttachmentUri(accountId, attachment.getAttachmentId());
            Uri contentUri =
                EmAttachmentProvider.resolveAttachmentIdToContentUri(resolver, attachmentUri);
            // Regexp which matches ' src="cid:contentId"'.
            String contentIdRe = "\\s+(?i)src=\"cid(?-i):\\Q" + contentId + "\\E\"";
            // Replace all occurrences of src attribute with ' src="content://contentUri"'.
            text = text.replaceAll(contentIdRe, " src=\"" + contentUri + "\""); 
        }

        if (part.getBody() instanceof EmMultipart) {
            EmMultipart mp = (EmMultipart)part.getBody();
            for (int i = 0; i < mp.getCount(); i++) {
                text = resolveInlineImage(resolver, accountId, text, mp.getBodyPart(i), depth + 1);
            }
        }

        return text;
    }

    /**
     * Escape some special character as HTML escape sequence.
     * 
     * @param text Text to be displayed using WebView.
     * @return Text correctly escaped.
     */
    public static String escapeCharacterToDisplay(String text) {
        Pattern pattern = PLAIN_TEXT_TO_ESCAPE;
        Matcher match = pattern.matcher(text);
        
        if (match.find()) {
            StringBuilder out = new StringBuilder();
            int end = 0;
            do {
                int start = match.start();
                out.append(text.substring(end, start));
                end = match.end();
                int c = text.codePointAt(start);
                if (c == ' ') {
                    // Escape successive spaces into series of "&nbsp;".
                    for (int i = 1, n = end - start; i < n; ++i) {
                        out.append("&nbsp;");
                    }
                    out.append(' ');
                } else if (c == '\r' || c == '\n') {
                    out.append("<br>");
                } else if (c == '<') {
                    out.append("&lt;");
                } else if (c == '>') {
                    out.append("&gt;");
                } else if (c == '&') {
                    out.append("&amp;");
                }
            } while (match.find());
            out.append(text.substring(end));
            text = out.toString();
        }        
        return text;
    }
}
