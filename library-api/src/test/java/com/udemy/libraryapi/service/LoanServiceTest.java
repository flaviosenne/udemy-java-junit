package com.udemy.libraryapi.service;

import com.udemy.libraryapi.domain.entity.Book;
import com.udemy.libraryapi.domain.entity.Loan;
import com.udemy.libraryapi.exception.BusinessException;
import com.udemy.libraryapi.model.repository.LoanRepository;
import com.udemy.libraryapi.service.impl.LoanServiceImpl;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class LoanServiceTest {

    @MockBean
    LoanRepository repository;

    LoanService service;

    @BeforeEach
    void setup(){
        service = new LoanServiceImpl(repository);
    }


    @Test
    @DisplayName("Should save a loan")
    void saveLoanTest(){

        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                .book(book)
                .loanDate(LocalDate.now())
                .customer(customer)
                .build();

        Loan savedLoan = Loan.builder()
                .id(1l)
                .book(book)
                .loanDate(LocalDate.now())
                .customer(customer)
                .build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(false);

        when(repository.save(savingLoan)).thenReturn(savedLoan);

        Loan loan= service.save(savingLoan);

        BDDAssertions.assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        BDDAssertions.assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        BDDAssertions.assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        BDDAssertions.assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Should throw exception when save a loan already loan")
    void saveLoanFailTest(){

        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                .book(book)
                .loanDate(LocalDate.now())
                .customer(customer)
                .build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() ->service.save(savingLoan));

        BDDAssertions.assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        verify(repository, never()).save(savingLoan);
    }

    @Test
    @DisplayName("Should get informations of loan by id")
    void getLoanDetailsTest(){
        Long id = 1l;
        Loan loan = createLoan();
        loan.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(loan));

        Optional<Loan> result = service.getById(id);

        BDDAssertions.assertThat(result).isPresent();
        BDDAssertions.assertThat(result.get().getId()).isEqualTo(id);
        BDDAssertions.assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        BDDAssertions.assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        BDDAssertions.assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(repository).findById(id);
    }

    public Loan createLoan(){
        Book book = Book.builder().id(1l).build();
        String customer = "Fulano";

        return Loan.builder()
                .book(book)
                .loanDate(LocalDate.now())
                .customer(customer)
                .build();
    }
}
