import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { CapitalappSharedModule } from 'app/shared';
import {
    ReportComponent,
    ReportDetailComponent,
    ReportUpdateComponent,
    ReportDeletePopupComponent,
    ReportDeleteDialogComponent,
    reportRoute,
    reportPopupRoute
} from './';

const ENTITY_STATES = [...reportRoute, ...reportPopupRoute];

@NgModule({
    imports: [CapitalappSharedModule, RouterModule.forChild(ENTITY_STATES)],
    declarations: [ReportComponent, ReportDetailComponent, ReportUpdateComponent, ReportDeleteDialogComponent, ReportDeletePopupComponent],
    entryComponents: [ReportComponent, ReportUpdateComponent, ReportDeleteDialogComponent, ReportDeletePopupComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class CapitalappReportModule {}
