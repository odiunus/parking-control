package com.api.parkingcontrol.services;

import com.api.parkingcontrol.dtos.ParkingSpoTDtoResponse;
import com.api.parkingcontrol.dtos.ParkingSpotDtoRequest;
import com.api.parkingcontrol.exception.GenericConflictException;
import com.api.parkingcontrol.exception.GenericExceptionNotFound;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.repositores.ParkingSpotRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ParkingSpotServices {

    final   ParkingSpotRepository parkingSpotRepository;
    @Autowired
    ModelMapper modelMapper = new ModelMapper();

    public ParkingSpotServices (ParkingSpotRepository parkingSpotRepository) {
        this.parkingSpotRepository = parkingSpotRepository;
    }

    @Transactional
    public ParkingSpotModel savePSM(ParkingSpotDtoRequest parkingSpotDto) {

        ParkingSpotModel parkingSpotModel = new ParkingSpotModel();

        if (existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())) {
            throw new GenericConflictException("Conflict: License Plate Car is already in use !");
        }

        if (existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
            throw new GenericConflictException("Conflict: Parking Spot Number is already in use !");
        }
        if (existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())) {
            throw new GenericConflictException("Conflict: Parking Spot already registered for this apartment/block !");
        }
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        return parkingSpotRepository.save(parkingSpotModel);
    }

    @Transactional
    public ParkingSpoTDtoResponse update(UUID id, ParkingSpotDtoRequest parkingSpotDto) {


        ParkingSpotModel parkingSpotModel = parkingSpotRepository.findById(id).
                orElseThrow(() -> new GenericExceptionNotFound("Parking Spot Model Not Found!"));

            BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
            parkingSpotModel.setId(parkingSpotModel.getId());
            parkingSpotModel.setRegistrationDate(parkingSpotModel.getRegistrationDate());

        return convertEntity(parkingSpotModel);
    }

    public ParkingSpoTDtoResponse findById(UUID id) {
        ParkingSpotModel parkingSpotModel = parkingSpotRepository.findById(id).
                orElseThrow(() -> new GenericExceptionNotFound("Parking Spot Model Not Found!"));

        return convertEntity(parkingSpotModel);
    }


    public boolean existsByLicensePlateCar(String LicensePlateCar){
        return parkingSpotRepository.existsByLicensePlateCar(LicensePlateCar);
    }

    public  boolean existsByParkingSpotNumber(String parkingSpotNumber){
        return  parkingSpotRepository.existsByParkingSpotNumber(parkingSpotNumber);
    }

    public boolean existsByApartmentAndBlock(String apartment,String block){
        return  parkingSpotRepository.existsByApartmentAndBlock(apartment,block);
    }
    public Page<ParkingSpoTDtoResponse> findAll(Pageable pageable) {
        return parkingSpoTDtoResponseList(parkingSpotRepository.findAll(pageable));
    }

    @Transactional
    public ResponseEntity<String> delete(UUID id) {
        Optional<ParkingSpotModel> parkingSpotModelOptional = Optional.ofNullable(parkingSpotRepository.findById(id).
                orElseThrow(() -> new GenericExceptionNotFound("Parking Spot Model Not Found!")));
        parkingSpotRepository.delete(parkingSpotModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Delete Sucefully!");
    }

    public List<ParkingSpotModel> findAllParkingSpot() {
        return parkingSpotRepository.findAllQuery();
    }

    public List<String> findAllBlock() {
        return parkingSpotRepository.findAllQueryBlock();
    }

    public List<ParkingSpotModel> findAllParkingSpotByBlock(String block) {
        return parkingSpotRepository.findAllParkingSpot(block);
    }

    public List<ParkingSpotModel> findPSMblocks(String block) {
             return parkingSpotRepository.findByblock(block);
    }

    public ParkingSpoTDtoResponse convertEntity(ParkingSpotModel parkingSpotModel){
       return modelMapper.map(parkingSpotModel, ParkingSpoTDtoResponse.class);
    }

    public Page<ParkingSpoTDtoResponse> parkingSpoTDtoResponseList(Page<ParkingSpotModel> parkingSpotModels){
        List<ParkingSpoTDtoResponse> lista = parkingSpotModels.stream()
                .map(this::convertEntity)
                .collect(Collectors.toList());
        return (Page<ParkingSpoTDtoResponse>) lista;
    }



}
