package com.secureskytech.multipartcsrfgen;

import java.nio.charset.Charset;
import java.util.Objects;

import javax.servlet.http.Part;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.secureskytech.multipartcsrfgen.CsrfHistory.CsrfItem;

import lombok.Data;

@Controller
@SessionAttributes("csrfHistory")
public class IndexController {

    @Autowired
    ApplicationContext applicationContext;

    @ModelAttribute("csrfHistory")
    public CsrfHistory createCsrfHistory() {
        return new CsrfHistory();
    }

    @RequestMapping("/")
    public String index(Model m) {
        String[] charsets = new String[] { "UTF-8", "Shift_JIS", "windows-31j", "EUC-JP", "ISO-2022-JP", "ISO-8859-1" };
        m.addAttribute("charsets", charsets);
        return "index";
    }

    @RequestMapping("/dummy")
    public String dummy(Model m) {
        return index(m);
    }

    @RequestMapping("/clear-history")
    public String clearHistory(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }

    @Data
    public static class UploadRequestForm {
        @NotEmpty
        public String url;
        @NotEmpty
        public String csname;
        public boolean enableAutoAccess;
        public boolean withCredentials;
        public boolean copyAuthorizationHeader;
    }

    @PostMapping("/upload-multipart-request")
    public String uploadMultipartRequest(UploadRequestForm uploadRequestForm, @RequestParam Part httpRequest, Model m,
            CsrfHistory csrfHistory) throws Exception {
        final Charset charset = Charset.forName(uploadRequestForm.csname);

        m.addAttribute("httpRequest", httpRequest); // TODO アップロードされない場合は、nullにはならない。size = -1 になるのかな？
        System.out.println("===============>>>>>>>>>>>> uploaded Part.size(); = [[[[" + httpRequest.getSize() + "]]]"); // TODO
        byte[] httpRequestAsBytes = StreamUtils.copyToByteArray(httpRequest.getInputStream());
        final HttpMultipartRequest mpreq = HttpMultipartRequest.parse(httpRequestAsBytes, charset);
        final String csrfHtml =
            mpreq.createCSRFhtml(
                uploadRequestForm.url,
                uploadRequestForm.enableAutoAccess,
                uploadRequestForm.withCredentials,
                uploadRequestForm.copyAuthorizationHeader);
        CsrfItem csrfItem = new CsrfItem(uploadRequestForm.url, charset, mpreq, csrfHtml);
        csrfHistory.items.add(0, csrfItem);
        m.addAttribute("csrfItem", csrfItem);
        return "csrf-form";
    }

    @RequestMapping("/csrf-form/{token}")
    public String csrfForm(@PathVariable String token, Model m, CsrfHistory csrfHistory) {
        for (CsrfItem i : csrfHistory.items) {
            if (i.token.equals(token)) {
                m.addAttribute("csrfItem", i);
                break;
            }
        }
        return "csrf-form";
    }

    @RequestMapping("/csrf-form-attack/{token}")
    @ResponseBody
    public ResponseEntity<byte[]> csrfFormAttack(@PathVariable String token, Model m, CsrfHistory csrfHistory) {
        CsrfItem csrfItem = null;
        for (CsrfItem i : csrfHistory.items) {
            if (i.token.equals(token)) {
                csrfItem = i;
                break;
            }
        }
        if (Objects.isNull(csrfItem)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new byte[0]);
        }
        final byte[] body = csrfItem.csrfHtml.getBytes(csrfItem.charset);
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.parseMediaType("text/html; charset=" + csrfItem.charset.name()))
            .body(body);
    }
}
