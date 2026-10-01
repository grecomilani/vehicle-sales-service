package com.grecomilani.vehiclesales.domain.sale;

import com.grecomilani.vehiclesales.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfTest {

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "529.982.247-25", " 529 982 247 25 "})
    void shouldAcceptValidCpfWithOrWithoutMask(String input) {
        assertThat(new Cpf(input).value()).isEqualTo("52998224725");
    }

    @Test
    void shouldAcceptCpfWhoseCheckDigitIsZero() {
        // no 123.456.789-09 o cálculo do primeiro dígito verificador dá resto 10, que vira 0
        assertThat(new Cpf("123.456.789-09").value()).isEqualTo("12345678909");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "5299822472",      // 10 dígitos
            "529982247250",    // 12 dígitos
            "5299822472a",     // letra
            "11111111111",     // todos os dígitos iguais
            "52998224735",     // primeiro dígito verificador errado
            "52998224726"      // segundo dígito verificador errado
    })
    void shouldRejectInvalidCpf(String input) {
        assertThatThrownBy(() -> new Cpf(input))
                .isInstanceOf(DomainException.class)
                .hasMessage("CPF inválido.");
    }
}
