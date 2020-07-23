package self.licw.o2o.controller.superadmin;

import self.licw.o2o.entity.HeadLine;
import self.licw.o2o.entity.dto.Result;
import self.licw.o2o.service.solo.HeadLineService;
import self.licw.simpleframework.core.annotation.Controller;
import self.licw.simpleframework.inject.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class HeadLineOperationController {
    @Autowired
    private HeadLineService headLineService;

    public Result<List<HeadLine>> getHeadLineList(HttpServletRequest request, HttpServletResponse response){
        return headLineService.getHeadLineList(new HeadLine());
    }

    public Result<Boolean> addHeadLine(HttpServletRequest request, HttpServletResponse response){
        return headLineService.addHeadLine(new HeadLine());
    }

    public Result<Boolean> removeHeadLine(HttpServletRequest request, HttpServletResponse response) {
        return headLineService.removeHeadLine(1);
    }
    public Result<Boolean> modifyHeadLine(HttpServletRequest request, HttpServletResponse response){
        return headLineService.modifyHeadLine(new HeadLine());
    }
    public Result<HeadLine> getHeadLinebyId(int headLineId){
        return headLineService.getHeadLinebyId(1);
    }
}
