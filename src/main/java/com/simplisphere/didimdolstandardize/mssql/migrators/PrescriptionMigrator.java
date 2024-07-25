package com.simplisphere.didimdolstandardize.mssql.migrators;

import com.simplisphere.didimdolstandardize.mssql.entities.MsPlanDetail;
import com.simplisphere.didimdolstandardize.mssql.repositories.MsChartRepository;
import com.simplisphere.didimdolstandardize.mssql.services.MsPlanService;
import com.simplisphere.didimdolstandardize.postgresql.RuleType;
import com.simplisphere.didimdolstandardize.postgresql.entities.Chart;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.entities.StandardizedRule;
import com.simplisphere.didimdolstandardize.postgresql.entities.prescription.Medicine;
import com.simplisphere.didimdolstandardize.postgresql.entities.prescription.Prescription;
import com.simplisphere.didimdolstandardize.postgresql.repositories.ChartRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.MedicineRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrescriptionMigrator {
    private final MsPlanService msPlanService;
    private final RuleRepository ruleRepository;
    private final MedicineRepository medicineRepository;
    private final MsChartRepository msChartRepository;
    private final ChartRepository chartRepository;

    public Page<Prescription> convertPrescription(Hospital hospital, Pageable pageRequest) {
        // original SosulTrxData(PO | insul) list 조회
        Page<MsPlanDetail> legacyPrescriptionPage = msPlanService.retrievePlanDetails(pageRequest);

        // 필요한 Chart의 ID와 Medicine의 이름을 Set으로 수집
        Set<String> chartIds = legacyPrescriptionPage.stream()
                .map(data -> data.getId().toString() + "01")
                .collect(Collectors.toSet());

        Set<String> medicineNames = legacyPrescriptionPage.stream()
                .map(MsPlanDetail::getDesc)
                .collect(Collectors.toSet());

        // 모든 필요한 Chart와 Medicine을 미리 조회하여 맵으로 저장
        Map<String, Chart> chartMap = chartRepository.findByOriginalIdIn(chartIds).stream()
                .collect(Collectors.toMap(Chart::getOriginalId, chart -> chart));

        List<StandardizedRule> ruleList = ruleRepository.findByTypeAndFromNameInAndHospital(RuleType.PRESCRIPTION, medicineNames, hospital);
        Set<String> ruleToNames = ruleList.stream()
                .map(StandardizedRule::getToName)
                .collect(Collectors.toSet());

        Map<String, Medicine> medicineMap = medicineRepository.findByNameIn(ruleToNames).stream()
                .collect(Collectors.toMap(Medicine::getName, medicine -> medicine));

        Map<String, StandardizedRule> ruleMap = ruleList.stream().collect(Collectors.toMap(StandardizedRule::getFromName, rule -> rule));

        // original SosulTrxData -> Prescription 변환
        List<Prescription> newPrescriptions = legacyPrescriptionPage.stream().parallel().map(legacyPrescription -> {
            String chartOriginalId = legacyPrescription.getId().toString() + "01";
            String prescriptionOriginalId = legacyPrescription.getId() + StringUtils.leftPad(legacyPrescription.getListOrder().toString(), 2, "0");
            Chart chart = chartMap.get(chartOriginalId);
            Patient patient = chart != null ? chart.getPatient() : null;

            // rule이 존재한다면 미리 조회한 medicine 맵에서 값을 가져와 대입
            Medicine medicine = Optional.ofNullable(ruleMap.get(legacyPrescription.getDesc().trim()))
                    .map(rule -> medicineMap.get(rule.getToName()))
                    .orElse(null);

            Integer days = legacyPrescription.getDays();
            Integer total = legacyPrescription.getDays() * legacyPrescription.getDosagePerDay();
            String DoctorName = legacyPrescription.getDoctor() == null ? "" : legacyPrescription.getDoctor().getName().trim();

            return Prescription.builder()
                    .code(legacyPrescription.getCode())
                    .name(legacyPrescription.getDesc())
                    .days(days)
                    .dosePerDay(legacyPrescription.getDosagePerDay())
                    .total(total)
                    .unit(legacyPrescription.getUnitDosage())
                    .doctor(DoctorName)
                    .originalId(prescriptionOriginalId)
                    .medicine(medicine)
                    .chart(chart)
                    .patient(patient)
                    .hospital(hospital)
                    .created(legacyPrescription.getCreatedAt())
                    .updated(legacyPrescription.getCreatedAt())
                    .build();
        }).toList();

        return new PageImpl<>(newPrescriptions, legacyPrescriptionPage.getPageable(), legacyPrescriptionPage.getTotalElements());
    }
}
