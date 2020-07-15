package self.licw.o2o.service.solo.impl;

import self.licw.o2o.entity.HeadLine;
import self.licw.o2o.entity.dto.Result;
import self.licw.o2o.service.solo.HeadLineService;
import self.licw.simpleframework.core.annotation.Service;

import java.util.List;

@Service
public class HeadLineServiceImpl implements HeadLineService {
    @Override
    public Result<List<HeadLine>> getHeadLineList(HeadLine headLineCondition) {
        return null;
    }

    @Override
    public Result<Boolean> addHeadLine(HeadLine headLine) {
        return null;
    }

    @Override
    public Result<Boolean> removeHeadLine(int headLineId) {
        return null;
    }

    @Override
    public Result<Boolean> modifyHeadLine(HeadLine headLine) {
        return null;
    }

    @Override
    public Result<HeadLine> getHeadLinebyId(int headLineId) {
        return null;
    }
}
