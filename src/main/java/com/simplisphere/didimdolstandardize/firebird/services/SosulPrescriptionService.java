package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulTrxData;
import com.simplisphere.didimdolstandardize.firebird.repositories.SosulTrxDataRepository;
import com.simplisphere.didimdolstandardize.postgresql.RuleType;
import com.simplisphere.didimdolstandardize.postgresql.entities.Chart;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.entities.StandardizedRule;
import com.simplisphere.didimdolstandardize.postgresql.entities.prescription.Medicine;
import com.simplisphere.didimdolstandardize.postgresql.entities.prescription.Prescription;
import com.simplisphere.didimdolstandardize.postgresql.repositories.ChartRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.MedicineRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.PatientRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class SosulPrescriptionService {

    private final SosulTrxDataRepository sosulTrxDataRepository;
    private final ChartRepository chartRepository;
    private final PatientRepository patientRepository;
    private final RuleRepository ruleRepository;
    private final MedicineRepository medicineRepository;

    public Page<Prescription> convertPrescriptionFromHospitalPrescription(Hospital hospital, PageRequest pageRequest) {
        // original SosulTrxData(PO | insul) list 조회
        Page<SosulTrxData> sosulTrxDataList = sosulTrxDataRepository.findByRouteOrName("PO", "insul", pageRequest);

        // 필요한 Chart의 ID와 Medicine의 이름을 Set으로 수집
        Set<String> chartIds = sosulTrxDataList.stream()
                .map(data -> data.getChart().getId().toString())
                .collect(Collectors.toSet());

        Set<String> medicineNames = sosulTrxDataList.stream()
                .map(SosulTrxData::getName)
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
        List<Prescription> newPrescriptions = sosulTrxDataList.stream().parallel().map(sosulTrxData -> {
            Chart chart = chartMap.get(sosulTrxData.getChart().getId().toString());
            Patient patient = chart != null ? chart.getPatient() : null;

            // rule이 존재한다면 미리 조회한 medicine 맵에서 값을 가져와 대입
            Medicine medicine = Optional.ofNullable(ruleMap.get(sosulTrxData.getName()))
                    .map(rule -> medicineMap.get(rule.getToName()))
                    .orElse(null);

            Integer days = (sosulTrxData.getTotal() != null && sosulTrxData.getPerDay() != null && sosulTrxData.getPerDay() != 0)
                    ? sosulTrxData.getTotal() / sosulTrxData.getPerDay()
                    : 0;

            return Prescription.builder()
                    .code("")
                    .name(sosulTrxData.getName())
                    .days(days)
                    .dosePerDay(sosulTrxData.getPerDay())
                    .total(sosulTrxData.getTotal())
                    .unit(sosulTrxData.getUnit())
                    .doctor(sosulTrxData.getSign())
                    .originalId(sosulTrxData.getId().toString())
                    .medicine(medicine)
                    .chart(chart)
                    .patient(patient)
                    .hospital(hospital)
                    .created(sosulTrxData.getChart().getDate().atTime(sosulTrxData.getChart().getTime()))
                    .updated(sosulTrxData.getChart().getDate().atTime(sosulTrxData.getChart().getTime()))
                    .build();
        }).toList();

        return new PageImpl<>(newPrescriptions, sosulTrxDataList.getPageable(), sosulTrxDataList.getTotalElements());
    }
}
