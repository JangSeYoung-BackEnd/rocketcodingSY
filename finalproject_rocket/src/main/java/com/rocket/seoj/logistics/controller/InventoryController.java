package com.rocket.seoj.logistics.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocket.jsy.employee.model.dto.Employee;
import com.rocket.seoj.logistics.model.dto.Inventory;
import com.rocket.seoj.logistics.model.dto.InventoryAttach;
import com.rocket.seoj.logistics.model.dto.InventoryFileWrapper;
import com.rocket.seoj.logistics.model.dto.PrdInventory;
import com.rocket.seoj.logistics.model.service.InventoryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Brief description of functions
 *
 * @author J
 * @version 2023-12-25
 */
@Controller
/*@RequestMapping("/logistics")*/
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService service;

	/*	@ExceptionHandler (DataAccessException.class)
		public ResponseEntity<Object>
		       handleDataAccessException(DataAccessException ex,
		                                 WebRequest request) {
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("timestamp", LocalDateTime.now());
			body.put("message", "업데이트 실패");

			return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
		}*/

    private Map<String, String> tableIdColumnMap = new HashMap<String, String>() {

        {
            put("EMPLOYEE", "EMP_NO");
            put("INVENTORY", "IV_ID");
            put("BRANCH", "BRANCH_ID");
            put("STOCK", "STK_ID");
            put("PUBLISHER", "PUB_ID");
            put("PRODUCT", "PRD_ID");
            put("PRD_ATTACH", "PRD_FILE_ID");
            put("PRDINVENTORY", "PRD_IV_ID");
            put("INVENTORY_ATTACH", "IV_FILE_ID");
            put("TAG", "DOC_TAG");
            put("DOCUMENT", "DOC_NO");
            put("APPROVAL", "APRV_NO");
            put("APPROVAL_FILE", "APRVFILE_NO");
        }
    };

    @PostMapping("/logistics/inventory/list/tableupdate")
    public ResponseEntity<?> updateTable(SQLException ex,
                                         @RequestParam("id") long id,
                                         @RequestParam("columnName") String columnName,
                                         @RequestParam("tableName") String tableName,
                                         @RequestParam("value") String value,
                                         @RequestParam("parentTableName") String parentTableName,
                                         @RequestParam("parentColumnName") String parentColumnName) throws DataAccessException {
//        log.debug("parentTableName : " + parentTableName);

        String parentColumnId = tableIdColumnMap.get(parentTableName.toUpperCase());
        String columnId = tableIdColumnMap.get(tableName);

        if (parentColumnName.equals("IV_VAT_TYPE")) {
            value = value.toUpperCase();
        }

//        log.debug("ㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇ컨트롤러ㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇ" + String.valueOf(
//                id) + ", " + columnName + ", " + tableName + ", " + value + ", " + parentTableName + ", " + parentColumnId + ", " + parentColumnName + ", " + columnId);

//        log.debug("업데이트 실행합니다");
        int result = 0;

        try {

            result = service.updateColumn(id, columnName, tableName.toUpperCase(), value, parentTableName.toUpperCase(),
                                          parentColumnId, parentColumnName, columnId);
//            log.debug("result : " + result);

            if (result > 0) {

                return ResponseEntity
                        .ok()
                        .body("업데이트 성공");
            } else {

                return ResponseEntity
                        .badRequest()
                        .body("업데이트 실패");
            }
        } catch (DataAccessException e) {
            return ResponseEntity
                    .badRequest()
                    .body("업데이트 실패");
        }

    }

    @RequestMapping("/logistics/inventory/write/branchempinfo")
    public ResponseEntity<?> branchempinfo(@RequestParam("branchId") long branchId) {
        List<Map<String, Object>> empInfoBybrc = service.branchempinfo(branchId);

        if (empInfoBybrc.isEmpty()) {
            return ResponseEntity
                    .noContent()
                    .build();
        }
//        log.debug("brcInfo: {}", empInfoBybrc);
        return ResponseEntity.ok(empInfoBybrc);

    }


    @RequestMapping("/logistics/inventory/write/prdinfo")
    public ResponseEntity<?> prdinfo(@RequestParam("prdId") long id) {
        Map<String, Object> prdInfo = service.getProductInfo(id);

        if (prdInfo.isEmpty()) {
            return ResponseEntity
                    .noContent()
                    .build();
        }

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US); // 미국식 숫자 포맷을 사용합니다.

        String[] keys = { "PRD_PRICE", "PRICE_IN_STK" };
        for (String key : keys) {
            if (prdInfo.containsKey(key) && prdInfo.get(key) != null) {
                try {
                    // 값이 숫자로 가정하고 처리합니다.
                    Number number = (Number)prdInfo.get(key);
                    String formatted = formatter.format(number);
                    prdInfo.put(key, formatted); // 포맷된 문자열로 다시 넣습니다.
                } catch (ClassCastException e) {
                    // 숫자가 아닐 경우 예외 처리
                    System.err.println(key + " is not a number.");
                }
            }
        }


//        log.debug("prdInfo: {}", prdInfo);
        return ResponseEntity.ok(prdInfo);

    }

    public List<PrdInventory> convertJsonToPrdInventoryList(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, new TypeReference<List<PrdInventory>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            // 오류 처리 또는 빈 리스트 반환
            return Collections.emptyList();
        }
    }

    @PostMapping("/logistics/inventory/endwrite")
    public String endWriteInventory(@RequestParam(name = "files", required = false) MultipartFile[] upFile,
//                                               List<PrdInventory> prdInventory,
                                    @RequestParam("prdInventory") String prdInventory,
//                                               @ModelAttribute PrdInventoryWrapper prdInventory,
//                                               @RequestBody List<PrdInventory> prdInventory,
//                                               @RequestParam("tableData") String tableData,
                                    Inventory inv,
                                    InventoryFileWrapper invAttach,
                                    Model model,
                                    HttpSession session,
                                    @ModelAttribute Inventory formData,
//                                               @RequestParam("aprvEmp") String aprvEmpList,
                                    @RequestParam(name = "inventoryInfoForCreateDocument", required = false) ArrayList<Map<String, Object>> inventoryInfoForCreateDocument
            /*, RedirectAttributes redirectAttributes*/

//                                               @RequestParam("formData") Object formData,
    ) {

        Employee loginemp = (Employee)SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        model.addAttribute("loginemp", loginemp);


//        log.debug("tableData: " + tableData[0]['prdId']);
//        log.debug("ㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹㄹ" + aprvEmpList);
        List<PrdInventory> prdInventoryList = convertJsonToPrdInventoryList(prdInventory);

        String path = session
                .getServletContext()
                .getRealPath("/resources/upload/logistics");
/*        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(formData);
            log.debug("JSON: " + json);
            // 이제 json 변수에 formData의 JSON 표현이 저장되어 있습니다.
        } catch (Exception e) {
            e.printStackTrace();
            // JSON 변환 중 발생한 오류 처리
        }*/

//        log.debug("ivType: " + ivType);
//        printFieldNames(formData);
//        log.debug("formData: " + formData.toString());
      /*  log.debug("formData: " + formData
                .getClass()
                .getName());
*/
        formData.setSendEmpId(loginemp.getEmpNo());
        long generatedId = service.insertInventory(formData);
//        log.debug("generatedId: " + generatedId);


        List<InventoryAttach> fileList = new ArrayList<>();

        if (upFile != null) {
            for (MultipartFile mf : upFile) {
//				if(!upFile.isEmpty()) {
                if (!mf.isEmpty()) {
//					String oriName=upFile.getOriginalFilename();
                    String oriName = mf.getOriginalFilename();
                    String ext = oriName.substring(oriName.lastIndexOf("."));
                    Date today = new Date(System.currentTimeMillis());
                    int randomNum = (int)(Math.random() * 10000) + 1;
                    String rename = "Rocket_Inventory_File_" + (new SimpleDateFormat("yyyyMMddHHmmssSSS").format(
                            today)) + "_" + randomNum + ext;
                    try {
//						upFile.transferTo(new File(path,rename));
                        mf.transferTo(new File(path, rename));
                        InventoryAttach file = InventoryAttach
                                .builder()
                                .ivId(generatedId)
                                .ivFileNameOri(oriName)
                                .ivFileNameRe(rename)
                                .ivAttachIsdel("N")
                                .build();

                        fileList.add(file);
                    } catch (IOException | DataAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
//        log.debug("컨트롤러 inv : " + inv);
//        log.debug("컨트롤러 invAttach : " + invAttach);
//        log.debug("컨트롤러 formData : " + formData);
//        log.debug("formDataJson: " + (String)formData);
//        log.debug("컨트롤러 tableData: " + tableData);

//        log.debug("깐트롤라");
        formData.setSendEmpId(loginemp.getEmpNo());
        formData.setSendBrcId((long)loginemp.getBranchId());


//        log.debug("fileList" + fileList.size());
        List<Integer> result2 = null;
        if (upFile != null) {
            for (InventoryAttach attach : fileList) {
                attach.setIvId(generatedId);
            }


            // TODO @transactional 되서 List<Integer>를 반환 안해도 될거같은데.. 물어보기
            result2 = service.insertInventoryAttach(fileList);
//            log.debug(String.valueOf(result2));
        }
/*        if (result2 != null) {
            for (Integer result : result2) {
                if (result == 0) {
                    return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("message", "실패 : 파일 추가 실패", "status", "error"));
                }
            }
        }*/

//        List<PrdInventory> prdInventoryList = prdInventory.getPrdInventory();

        for (PrdInventory prdIv : prdInventoryList) {
            prdIv.setIvId(generatedId);
        }

        List<Integer> result3 = service.insertPrdInventory(prdInventoryList);
/*        for (Integer result : result3) {
            if (result == 0) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "실패 : 상품_입출고 추가 실패", "status", "error"));
            }
        }*/


//        result2.forEach(insertInventoryAttachResult -> log.debug("결과: " + insertInventoryAttachResult));


        if (generatedId > 0) {

            Map<String, Object> paramMap = new HashMap<>();

            paramMap.put("empNo", loginemp.getEmpNo()); // 첫 번째 파라미터

            inventoryInfoForCreateDocument = service.getInventoryInfoForCreateDocument(generatedId);

/*
            model.addAttribute("inventoryInfo", inventoryInfoForCreateDocument);
            redirectAttributes.addFlashAttribute("inventoryInfo", inventoryInfoForCreateDocument);*/
            session.setAttribute("inventoryInfo", inventoryInfoForCreateDocument);


            return "redirect:/docu/insertaprv";

        }
        return "redirect:/docu/insertaprv";
        /*    return ResponseEntity.ok(Map.of("url", "aprv/aprvwrite"));*/
//        return ResponseEntity.ok(tableData);
    }

    public void printFieldNames(Object obj) {
        Class<?> objClass = obj.getClass();
        Field[] fields = objClass.getDeclaredFields();

        log.debug("Field names of class " + objClass.getName() + ":");
        for (Field field : fields) {
            log.debug(field.getName());
        }
    }

    @RequestMapping("/logistics/inventory/write")
    public String inventoryWrite(Model model) {
		/*		List<Map> inventories = service.selectAllInventories();
				model.addAttribute("inventories", inventories);*/

        Employee loginemp = (Employee)SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        model.addAttribute("loginemp", loginemp);

        List<Map<String, Object>> empListByemployeeId = service.getEmpListByemployeeId();
        List<Map<String, Object>> branchList = service.selectAllBranch();
//        System.out.println(branchList.size());
        List<Map<String, Object>> writeInventoryItem = service.selectWriteInventory();
        List<Map<String, Object>> selectAllProduct = service.selectAllProduct();

        Map<Object, Object> prdTitleToIdMap = new HashMap<>(); // PRD_TITLE을 키로, PRD_ID를 값으로 하는 맵
        Set<Object> prdTitles = new HashSet<>(); // 중복 제거를 위한 Set


        for (Map<String, Object> item : writeInventoryItem) {
            Object title = item.get("PRD_TITLE");
            Object id = item.get("PRD_ID");
            if (title != null && prdTitles.add(title)) {
                prdTitleToIdMap.put(title, id);
            }

        }

        ObjectMapper prdTitleToJson = new ObjectMapper();

        try {
            String jsonMap = prdTitleToJson.writeValueAsString(prdTitleToIdMap);
//            log.debug(jsonMap);
            model.addAttribute("jsonMap", jsonMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


/*        // 각 Map의 모든 키-값 쌍을 반복하여 출력
        for (Map<String, Object> productMap : selectAllProduct) {
            System.out.println("----- New Product Map -----");
            for (Map.Entry<String, Object> entry : productMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                log.error("key : {}, value : {}", key, value);
            }
        }


        Map<Object, Object> prdTitleToIdMap2 = new HashMap<>(); // PRD_TITLE을 키로, PRD_ID를 값으로 하는 맵
        Set<Object> prdTitles2 = new HashSet<>(); // 중복 제거를 위한 Set
        for (Map<String, Object> item : selectAllProduct) {
            Object title = item.get("PRD_TITLE");
            Object id = item.get("PRD_ID");
            if (title != null && prdTitles2.add(title)) {
                prdTitleToIdMap2.put(title, id);
            }

        }

        ObjectMapper prdTitleToJson2 = new ObjectMapper();

        try {
            String jsonMap2 = prdTitleToJson2.writeValueAsString(prdTitleToIdMap2);
            log.error("ㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁ" + jsonMap2);
            model.addAttribute("jsonMap2", jsonMap2);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }*/
        model.addAttribute("selectAllProduct", selectAllProduct);
        model.addAttribute("empListByemployeeId", empListByemployeeId);
        model.addAttribute("branchList", branchList);
        model.addAttribute("prdTitleToIdMap", prdTitleToIdMap);
        model.addAttribute("writeInventoryItem", writeInventoryItem);

        return "logistics/inventoryWrite";
    }

    @RequestMapping("/logistics/inventory/list")
    public String selectAllInventories(Model model) {
		/*		List<Map> inventories = service.selectAllInventories();
				model.addAttribute("inventories", inventories);*/

        Employee loginemp = (Employee)SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        model.addAttribute("loginemp", loginemp);


        List<Map<String, Object>> inventories = service.selectAllInventories(loginemp.getBranchId());

        // iv_id 중복 제거를 위한 코드
        Set<Object> uniqueIvIds = new HashSet<>();
        List<Map<String, Object>> uniqueInventoryData = new ArrayList<>();

        for (Map<String, Object> data : inventories) {
            Object ivId = data.get("IV_ID");

            if (uniqueIvIds.add(ivId)) {
                uniqueInventoryData.add(data);
            }
        }
		
		/*		for (Map<String, Object> inventory : inventories) {
					Object ivDate = inventory.get("IV_DATE");
					
					if (ivDate != null) {
						log.debug("IV_DATE의 자료형: " + ivDate.getClass().getName());
					} else {
						log.debug("IV_DATE 키에 해당하는 값이 없습니다.");
					}
				}*/

        model.addAttribute("inventories", uniqueInventoryData);
        return "logistics/inventoryList";
    }

    @PostMapping("/logistics/inventory/list/delete")
    public ResponseEntity<?> deleteInventoryAndAttachments(@RequestParam("iv_id") Long inventoryId) {
//        log.debug("딜리트: " + inventoryId);
        boolean deletionSuccess = service.deleteInventoryAndAttachmentAndPrdIv(inventoryId);

        if (deletionSuccess) {

            return ResponseEntity
                    .ok()
                    .body(Map.of("message", "삭제 성공", "status", "success"));
        } else {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "삭제 실패", "status", "error"));
        }
    }

/*    @GetMapping("/inventories")
    @ResponseBody
    public List<Map<String, Object>> getInventories(Model model) {
        Employee loginemp = (Employee)SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        model.addAttribute("loginemp", loginemp);
        List<Map<String, Object>> items = service.selectAllInventories(loginemp.getBranchId());

        for (Map<String, Object> item : items) {

            for (Map.Entry<String, Object> entry : item.entrySet()) {

                if (entry.getValue() instanceof Clob) {
                    Clob clobData = (Clob)entry.getValue();
                    String convertedValue = clobToString(clobData);
                    entry.setValue(convertedValue);
                }
            }
        }

        return items;
    }*/

    public String clobToString(Clob data) {
        StringBuilder sb = new StringBuilder();

        try {
            Reader reader = data.getCharacterStream();
            BufferedReader br = new BufferedReader(reader);

            String line;

            while (null != (line = br.readLine())) {
                sb.append(line);
            }
            br.close();
        } catch (SQLException | IOException e) {

        }
        return sb.toString();
    }
	
	/*	@Controller
		@RequestMapping ("/logistics")
		@RequiredArgsConstructor
		public class InventoryController {
	
			private final InventoryService service;
	
			@GetMapping ("/List") @ResponseBody
			public Map<String, Object> getAllItems(Model model) {
				List<Inventory> inventories = service.selectAllInventories();
				model.addAttribute("inventories", inventories);
	
				Map<String, Object> response = new HashMap<>();
				response.put("data", inventories);
				response.put("recordsTotal", inventories.size());
				response.put("recordsFiltered", inventories.size());
				return response;
			}*/
	
	/*	@GetMapping ("/index")
		public ResponseEntity<List<Inventory>> getAllItems() {
			List<Inventory> items = service.selectAllInventories();
	
			if (items.isEmpty()) {
				return ResponseEntity.noContent().build();
			}
	
			return ResponseEntity.ok(items);
		}
	
	/*	@GetMapping
		public ResponseEntity<List<InventoryDto>> selectAllInventories() {
			InventoryDto dto = new InventoryDto();
			List<Inventory> items = service.selectAllInventories();
			List<InventoryDto> dtos
			   = items.stream().map(dto::of).collect(Collectors.toList());
	
			if (items.isEmpty()) {
				return ResponseEntity.noContent().build();
			}
	
			return ResponseEntity.ok(dtos);
		}*/

}
