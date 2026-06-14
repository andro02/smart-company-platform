package com.smartcompany.service.company;

import com.smartcompany.dto.CityDto;
import com.smartcompany.dto.CountryDto;
import com.smartcompany.dto.company.CompanyDetailDto;
import com.smartcompany.dto.company.CompanyRequestDto;
import com.smartcompany.exception.AppException;
import com.smartcompany.model.City;
import com.smartcompany.model.Country;
import com.smartcompany.model.company.Company;
import com.smartcompany.model.company.CompanyDocument;
import com.smartcompany.model.company.CompanyImage;
import com.smartcompany.model.company.CompanyStatus;
import com.smartcompany.model.User;
import com.smartcompany.repository.CityRepository;
import com.smartcompany.repository.CountryRepository;
import com.smartcompany.repository.UserRepository;
import com.smartcompany.repository.company.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;

    @Transactional
    public CompanyDetailDto createRequest(String ownerUsername, CompanyRequestDto dto) {
        User owner = userRepository.findById(ownerUsername)
            .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        Country country = countryRepository.findById(dto.countryId())
            .orElseThrow(() -> new AppException("Country not found", HttpStatus.NOT_FOUND));
        City city = cityRepository.findById(dto.cityId())
            .orElseThrow(() -> new AppException("City not found", HttpStatus.NOT_FOUND));

        Company company = new Company();
        company.setName(dto.name());
        company.setAddress(dto.address());
        company.setCountry(country);
        company.setCity(city);
        company.setLatitude(dto.latitude());
        company.setLongitude(dto.longitude());
        company.setOwner(owner);
        company.setStatus(CompanyStatus.PENDING);

        if (dto.imageKeys() != null) {
            for (String key : dto.imageKeys()) {
                CompanyImage img = new CompanyImage();
                img.setImageKey(key);
                img.setCompany(company);
                company.getImages().add(img);
            }
        }

        if (dto.documents() != null) {
            for (CompanyRequestDto.DocumentDto docDto : dto.documents()) {
                CompanyDocument doc = new CompanyDocument();
                doc.setFileKey(docDto.fileKey());
                doc.setFileType(docDto.fileType());
                doc.setCompany(company);
                company.getDocuments().add(doc);
            }
        }

        return toDto(companyRepository.save(company));
    }

    private CompanyDetailDto toDto(Company c) {
        List<String> imageKeys = c.getImages().stream()
            .map(CompanyImage::getImageKey).toList();
        List<CompanyDetailDto.DocumentDto> documents = c.getDocuments().stream()
            .map(d -> new CompanyDetailDto.DocumentDto(d.getFileKey(), d.getFileType()))
            .toList();

        return new CompanyDetailDto(
            c.getId(),
            c.getName(),
            c.getAddress(),
            new CountryDto(c.getCountry().getId(), c.getCountry().getName(), c.getCountry().getCode()),
            new CityDto(c.getCity().getId(), c.getCity().getName(), c.getCity().getCountry().getId()),
            c.getLatitude(),
            c.getLongitude(),
            c.getStatus().toString(),
            c.getRejectionReason(),
            c.getOwner().getUsername(),
            imageKeys,
            documents
        );
    }
}