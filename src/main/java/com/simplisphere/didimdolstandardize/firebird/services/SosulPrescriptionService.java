package com.simplisphere.didimdolstandardize.firebird.services;

import com.simplisphere.didimdolstandardize.firebird.entities.SosulTrxData;
import com.simplisphere.didimdolstandardize.firebird.repositories.SosulTrxDataRepository;
import com.simplisphere.didimdolstandardize.postgresql.RuleType;
import com.simplisphere.didimdolstandardize.postgresql.entities.Chart;
import com.simplisphere.didimdolstandardize.postgresql.entities.Hospital;
import com.simplisphere.didimdolstandardize.postgresql.entities.Patient;
import com.simplisphere.didimdolstandardize.postgresql.entities.prescription.Medicine;
import com.simplisphere.didimdolstandardize.postgresql.entities.prescription.Prescription;
import com.simplisphere.didimdolstandardize.postgresql.repositories.ChartRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.MedicineRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.PatientRepository;
import com.simplisphere.didimdolstandardize.postgresql.repositories.RuleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class SosulPrescriptionService {

    private final SosulTrxDataRepository sosulTrxDataRepository;
    private final ChartRepository chartRepository;
    private final PatientRepository patientRepository;
    private final RuleRepository ruleRepository;
    private final MedicineRepository medicineRepository;

    @Transactional
    public Page<Prescription> convertPrescriptionFromHospitalPrescription(Hospital hospital, PageRequest pageRequest) {
        // original SosulTrxData(PO | insul) list 조회
        Page<SosulTrxData> sosulTrxDataList = sosulTrxDataRepository.findByRouteOrName("PO", "insul", pageRequest);

        // original SosulTrxData -> Prescription 변환
        List<Prescription> newPrescriptions = sosulTrxDataList.stream().parallel().map(sosulTrxData -> {
                    Chart chart = Optional.ofNullable(sosulTrxData.getChart())
                            .map(chartData -> chartRepository.findByOriginalId(chartData.getId().toString()))
                            .orElse(null);

                    Patient patient = chart != null ? chart.getPatient() : null;

                    // rule이 존재한다면 medicine에서 rule.toName과 같은 이름을 가진 객체를 조회하여 대입
                    Medicine medicine = ruleRepository.findByTypeAndFromNameAndHospital(RuleType.PRESCRIPTION, sosulTrxData.getName(), hospital)
                            .flatMap(standardizedRule -> medicineRepository.findByName(standardizedRule.getToName()))
                            .orElse(null);
                    Integer days = (sosulTrxData.getTotal() != null && sosulTrxData.getPerDay() != null && sosulTrxData.getPerDay() != 0) ? sosulTrxData.getTotal() / sosulTrxData.getPerDay() : 0;
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
                })
                .toList();

        return new PageImpl<>(newPrescriptions, sosulTrxDataList.getPageable(), sosulTrxDataList.getTotalElements());
    }
}
