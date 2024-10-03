package com.nextuple.promoengine.mapper;

import com.nextuple.promoengine.dto.RuleDTO;
import com.nextuple.promoengine.model.Rule;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RuleMapper {

    RuleDTO ruleToRuleDTO(Rule rule);
    Rule ruleDTOToRule(RuleDTO ruleDTO);
}
