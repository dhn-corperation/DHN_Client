package com.dhn.client.controller;

import com.dhn.client.bean.*;
import com.dhn.client.service.TemplateReqSevice;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class TemplateRequest implements ApplicationListener<ContextRefreshedEvent> {

    public static boolean isStart = false;
    private boolean isCProc = false;
    private boolean isIProc = false;
    private boolean isDProc = false;
    private boolean isRProc = false;
    private SQLParameter param = new SQLParameter();
    private String dhnServer;
    private String userid;

    @Autowired
    private TemplateReqSevice templateReqSevice;

    @Autowired
    private ApplicationContext appContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        param.setTmp_table(appContext.getEnvironment().getProperty("dhnclient.tmp_table"));
        param.setBtn_table(appContext.getEnvironment().getProperty("dhnclient.btn_table"));
        param.setProfile_key(appContext.getEnvironment().getProperty("dhnclient.kakao_profile_key"));
        param.setMod_id((appContext.getEnvironment().getProperty("dhnclient.mod_id")));
        param.setTmp_use(appContext.getEnvironment().getProperty("dhnclient.tmp_use"));

        dhnServer = appContext.getEnvironment().getProperty("dhnclient.dhn_kakao_server");
        userid = appContext.getEnvironment().getProperty("dhnclient.userid");

        if (param.getTmp_use() != null && param.getTmp_use().equalsIgnoreCase("Y")) {
            log.info("Tmp 초기화 완료");
            isStart = true;
        }
    }

    @Scheduled(fixedDelay = 60000)
    private void CreateTemplate() {
        if(isStart && !isCProc) {
            isCProc = true;

            try{
                int cnt = templateReqSevice.selectTmplRequestCount(param);

                if(cnt > 0) {
                    log.info("템플릿 등록 시작");

                    List<TmplData> tmplDataList = templateReqSevice.selectTmplData(param);

                    for (TmplData tmplData : tmplDataList) {
                        param.setTmplid(tmplData.getTmplid());

                        TmplRequestBean tmplRequestBean = new TmplRequestBean();
                        tmplRequestBean.setSenderKey(tmplData.getSenderKey());
                        tmplRequestBean.setTemplateCode(tmplData.getTemplateCode());
                        tmplRequestBean.setTemplateName(tmplData.getTemplateName());
                        tmplRequestBean.setTemplateMessageType(tmplData.getTemplateMessageType());
                        tmplRequestBean.setTemplateEmphasizeType(tmplData.getTemplateEmphasizeType());
                        tmplRequestBean.setTemplateContent(tmplData.getTemplateContent());

                        if(tmplData.getTmpltype().equalsIgnoreCase("B")){
                            List<ButtonBean> btnList = templateReqSevice.selectBtnList(param);

                            tmplRequestBean.setButtons(btnList);
                        }

                        StringWriter sw = new StringWriter();
                        ObjectMapper om = new ObjectMapper();
                        om.writeValue(sw, tmplRequestBean);

                        HttpHeaders header = new HttpHeaders();

                        header.setContentType(MediaType.APPLICATION_JSON);
                        header.set("userid", userid);

                        RestTemplate rt = new RestTemplate();
                        HttpEntity<String> entity = new HttpEntity<String>(sw.toString(), header);

                        try {
                            ResponseEntity<String> response = rt.postForEntity(dhnServer + "template/create", entity, String.class);
                            Map<String, String> res = om.readValue(response.getBody().toString(), Map.class);
//                            log.info(res.toString());
                            if (response.getStatusCode() == HttpStatus.OK) {
                                if(res.get("code").equals("200")){
                                    log.info("템플릿 등록 완료 후 검수요청 시작(" + response.getStatusCode() + ")  템플릿 Code : " + tmplRequestBean.getTemplateCode());

                                    TmplinspectionBean tmplinspectionBean = new TmplinspectionBean();
                                    tmplinspectionBean.setSenderKey(tmplRequestBean.getSenderKey());
                                    tmplinspectionBean.setTemplateCode(tmplRequestBean.getTemplateCode());

                                    sw = new StringWriter();
                                    om = new ObjectMapper();
                                    om.writeValue(sw, tmplinspectionBean);

                                    rt = new RestTemplate();
                                    entity = new HttpEntity<String>(sw.toString(), header);

                                    try{
                                        response = rt.postForEntity(dhnServer + "/template/request", entity, String.class);
                                        res = om.readValue(response.getBody().toString(), Map.class);
//                                        log.info(res.toString());
                                        if (response.getStatusCode() == HttpStatus.OK) {
                                            if(res.get("code").equals("200")){
                                                templateReqSevice.updateTmplSuccess(param);
                                                log.info("템플릿 검수요청 완료(" + response.getStatusCode() + ") 템플릿 Code : "+ tmplinspectionBean.getTemplateCode());
                                            }else{
                                                templateReqSevice.updateTmplfail(param);
                                                log.info("템플릿 검수요청 오류(KAKAO) : " + tmplinspectionBean.getTemplateCode() + " / " + res.get("code") + " / " + res.get("message"));
                                            }
                                        }else{
                                            log.error("템플릿 검수요청 오류(Http ERR) : " + tmplinspectionBean.getTemplateCode() + " / " + res.toString());
                                        }
                                    }catch (Exception e){
                                        log.error("템플릿 검수요청 오류(Response) : " + tmplinspectionBean.getTemplateCode() + " / "  + e.toString());
                                    }

                                }else{
                                    templateReqSevice.updateTmplfail(param);
                                    log.info("템플릿 등록 오류(KAKAO) : " + tmplRequestBean.getTemplateCode() + " / "  + res.get("code") + " / " + res.get("message"));
                                }
                            } else {
                                log.error("템플릿 등록 오류(Http ERR) : "  + tmplRequestBean.getTemplateCode() + " / "  + res.toString());
                            }
                        } catch (Exception e) {
                            log.error("템플릿 등록 오류(Response) : "  + tmplRequestBean.getTemplateCode() + " / "  + e.toString());
                        }
                    }

                }

            }catch (Exception e){
                log.error("템플릿 등록 오류(Send) : " + e.toString());
            }
            isCProc = false;
        }
    }

    @Scheduled(cron = "0 30 * * * *")
    private void inspectionTemplate() {
        if(isStart && !isIProc) {
            isRProc = true;

            try{
                int cnt = templateReqSevice.selectInsRequestCount(param);

                if(cnt > 0) {
                    log.info("템플릿 검수결과 확인 시작");

                    List<TmplData> tmplList = templateReqSevice.selectTmplInsData(param);

                    for (TmplData tmplData : tmplList) {

                        RestTemplate rt = new RestTemplate();
                        ObjectMapper om = new ObjectMapper();

                        URI uri = UriComponentsBuilder
                                .fromUriString(dhnServer)
                                .path("template/")
                                .queryParam("senderKey",tmplData.getSenderKey())
                                .queryParam("templateCode",tmplData.getTemplateCode())
                                .queryParam("senderKeyType","S")
                                .encode()
                                .build()
                                .toUri();

                        param.setTmplid(tmplData.getTmplid());

                        try {
                            ResponseEntity<String> response = rt.getForEntity(uri, String.class);
                            Map<String, Object> res = om.readValue(response.getBody(), Map.class);

                            //log.info(res.toString());
                            if(res.get("code").equals("200")){
                                Map<String, Object> data = (Map<String, Object>) res.get("data");

                                String inspectionStatus = data.get("inspectionStatus").toString();
                                String status = data.get("status").toString();

                                if(inspectionStatus.equalsIgnoreCase("APR")){
                                    param.setTmplstatus(status);
                                    templateReqSevice.updateTmplInsAPR(param);
                                    log.info("템플릿 검수 승인 템플릿 Code : " + tmplData.getTemplateCode());
                                }else if(inspectionStatus.equalsIgnoreCase("REJ")){
                                    param.setTmplstatus(status);
                                    List<Map<String, Object>> comments = (List<Map<String, Object>>) data.get("comments");
                                    for (Map<String, Object> comment : comments) {
                                        param.setRej_memo(comment.get("content").toString());
                                    }
                                    templateReqSevice.updateTmplInsREJ(param);
                                    log.info("템플릿 검수 반려 템플릿코드 : " + tmplData.getTemplateCode());
                                }

                            }else{
                                log.error("템플릿 검수결과 조회 오류(Http ERR) : " + tmplData.getTemplateCode() + " / " + res.toString());
                            }
                        } catch (Exception e) {
                            log.error("템플릿 검수결과 조회 오류(Response) : " + tmplData.getTemplateCode() + " / " + e.toString());
                        }

                    }
                    log.info("템플릿 검수결과 확인 종료");
                }
            }catch (Exception e){
                log.error("템플릿 검수결과 확인 오류(Send) : " + e.toString());
            }
            isRProc = false;
        }
    }

    @Scheduled(cron = "0 0 1 * * *")
    private void refreshTemplate() {
        if(isStart && !isRProc) {
            isRProc = true;

            try{
                int cnt = templateReqSevice.selectRefreshTmplCount(param);

                if(cnt > 0){
                    log.info("템플릿 상태 갱신 시작");

                    List<TmplData> tmplList = templateReqSevice.selectTmplRefreshData(param);

                    for (TmplData tmplData : tmplList) {

                        RestTemplate rt = new RestTemplate();
                        ObjectMapper om = new ObjectMapper();

                        URI uri = UriComponentsBuilder
                                .fromUriString(dhnServer)
                                .path("template/")
                                .queryParam("senderKey",tmplData.getSenderKey())
                                .queryParam("templateCode",tmplData.getTemplateCode())
                                .queryParam("senderKeyType","S")
                                .encode()
                                .build()
                                .toUri();

                        param.setTmplid(tmplData.getTmplid());

                        try {
                            ResponseEntity<String> response = rt.getForEntity(uri, String.class);
                            Map<String, Object> res = om.readValue(response.getBody(), Map.class);

                            //log.info(res.toString());
                            if(res.get("code").equals("200")){
                                Map<String, Object> data = (Map<String, Object>) res.get("data");
                                //String inspectionStatus = data.get("inspectionStatus").toString();
                                String status = data.get("status").toString();
                                param.setTmplstatus(status);
                                templateReqSevice.updateTmplRefresh(param);
                                log.info("템플릿 상태 갱신 완료 템플릿코드 : " + tmplData.getTemplateCode());

                            }else{
                                log.error("템플릿 상태 갱신 조회 오류(Http ERR) : " + tmplData.getTemplateCode() + " / " + res.toString());
                            }
                        } catch (Exception e) {
                            log.error("템플릿 상태 갱신 조회 오류(Response) : " + tmplData.getTemplateCode() + " / " + e.toString());
                        }

                    }
                    log.info("템플릿 상태 갱신 종료 {}건",tmplList.size());
                }
            }catch (Exception e){
                log.error("템플릿 상태 갱신 오류(Send) : " + e.toString());
            }
            isRProc = false;
        }
    }
}
